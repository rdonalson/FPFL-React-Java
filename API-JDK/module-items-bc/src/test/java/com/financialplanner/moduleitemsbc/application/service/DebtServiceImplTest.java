package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.debt.DebtDraft;
import com.financialplanner.moduleitemsbc.domain.debt.DebtDraft.AllocationDraft;
import com.financialplanner.moduleitemsbc.domain.repository.DebtRepository;
import com.financialplanner.moduleitemsbc.domain.repository.ItemRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Port of API-NET's DebtServiceTests, against in-memory repositories. */
class DebtServiceImplTest {

    private static final UUID ME = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SOMEONE_ELSE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 18);

    private final Map<Long, Item> itemStore = new LinkedHashMap<>();
    private final Map<Long, Debt> debtStore = new LinkedHashMap<>();
    private DebtServiceImpl service;

    @BeforeEach
    void setUp() {
        add(item(10L, ME, "Chase Visa", 2, 200));
        add(item(11L, ME, "Car loan", 2, 350));
        add(item(20L, ME, "Paycheck", 1, 2000));
        add(item(21L, ME, "Bonus", 1, 500));
        add(item(30L, SOMEONE_ELSE, "Their card", 2, 100));
        add(item(31L, SOMEONE_ELSE, "Their paycheck", 1, 1000));

        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        service = new DebtServiceImpl(new InMemoryDebts(), new InMemoryItems(), clock);
    }

    private void add(Item item) {
        itemStore.put(item.getId(), item);
    }

    private static Item item(Long id, UUID owner, String name, long type, double amount) {
        Item item = new Item();
        item.setId(id);
        item.setUserId(owner);
        item.setName(name);
        item.setAmount(amount);
        item.setItemType(new ItemType(type, type == 1 ? "Credit" : "Debit"));
        item.setTimePeriod(new TimePeriod(6L, "Monthly"));
        item.setMonthlyDom(1);
        return item;
    }

    private static DebtDraft draft(Long itemId, String balance, String apr, AllocationDraft... allocations) {
        return new DebtDraft(itemId, balance == null ? null : new BigDecimal(balance),
                             apr == null ? null : new BigDecimal(apr), null, List.of(allocations));
    }

    private static AllocationDraft alloc(long creditId, String percent) {
        return new AllocationDraft(creditId, new BigDecimal(percent));
    }

    private static void assertInvalid(String expectedMessagePart, Runnable call) {
        DomainValidationException ex = assertThrows(DomainValidationException.class, call::run);
        assertTrue(ex.getMessage().contains(expectedMessagePart), ex.getMessage());
    }

    @Test
    void tracksADebitWithAllocationsAndSensibleDefaults() {
        Debt debt = service.create(draft(10L, "1500.555", null, alloc(20, "33.333")), ME);

        assertEquals(ME, debt.getUserId());
        assertEquals(10L, debt.getItem().getId());
        assertEquals(new BigDecimal("1500.56"), debt.getBalance());
        assertEquals(0, BigDecimal.ZERO.compareTo(debt.getApr()));
        assertEquals(TODAY, debt.getBalanceDate());
        assertEquals(1, debt.getAllocations().size());
        assertEquals(new BigDecimal("33.33"), debt.getAllocations().getFirst().getPercent());
    }

    @Test
    void onlyYourOwnDebitsCanBeTracked() {
        assertInvalid("Choose one of your debits", () -> service.create(draft(null, "100", null), ME));
        assertInvalid("Choose one of your debits", () -> service.create(draft(30L, "100", null), ME));
        assertInvalid("Only a debit", () -> service.create(draft(20L, "100", null), ME));
    }

    @Test
    void aDebitIsTrackedOnlyOnce() {
        service.create(draft(10L, "100", null), ME);
        assertThrows(DuplicateItemException.class, () -> service.create(draft(10L, "200", null), ME));
    }

    @Test
    void balanceAndAprMustBeInRange() {
        assertInvalid("Balance owed", () -> service.create(draft(10L, null, null), ME));
        assertInvalid("Balance owed", () -> service.create(draft(10L, "-1", null), ME));
        assertInvalid("Balance owed", () -> service.create(draft(10L, "1000000001", null), ME));
        assertInvalid("APR", () -> service.create(draft(10L, "100", "100.5"), ME));
        assertInvalid("APR", () -> service.create(draft(10L, "100", "-1"), ME));
    }

    @Test
    void allocationsMustBeYourCreditsWithASensiblePercentage() {
        assertInvalid("one of your credits", () -> service.create(draft(10L, "100", null, alloc(31, "10")), ME));
        assertInvalid("is not a credit", () -> service.create(draft(10L, "100", null, alloc(11, "10")), ME));
        assertInvalid("more than 0%", () -> service.create(draft(10L, "100", null, alloc(20, "0")), ME));
        assertInvalid("more than 0%", () -> service.create(draft(10L, "100", null, alloc(20, "100.01")), ME));
    }

    @Test
    void aCreditAppearsOncePerDebt() {
        assertInvalid("more than once",
                      () -> service.create(draft(10L, "100", null, alloc(20, "10"), alloc(20, "5")), ME));
    }

    @Test
    void aCreditCantBeMoreThan100PercentAllocatedAcrossDebts() {
        service.create(draft(10L, "100", null, alloc(20, "60")), ME);

        assertInvalid("already 60% allocated to other debts; at most 40% is left",
                      () -> service.create(draft(11L, "100", null, alloc(20, "50")), ME));
        assertEquals(1, service.create(draft(11L, "100", null, alloc(20, "40")), ME).getAllocations().size());
    }

    @Test
    void updateChangesTheBalanceAndAllocationsAndIgnoresItsOwnOldShare() {
        Debt created = service.create(draft(10L, "100", null, alloc(20, "90")), ME);
        DebtAllocation original = created.getAllocations().getFirst();

        Debt updated = service.update(created.getId(), draft(null, "250", "19.99", alloc(20, "100"), alloc(21, "50")),
                                      ME, false);

        assertEquals(new BigDecimal("250.00"), updated.getBalance());
        assertEquals(new BigDecimal("19.990"), updated.getApr());
        assertEquals(2, updated.getAllocations().size());
        // The paycheck's allocation is the same row, updated in place.
        assertSame(original, updated.getAllocations().getFirst());
        assertEquals(new BigDecimal("100.00"), original.getPercent());

        Debt trimmed = service.update(created.getId(), draft(null, "250", null, alloc(21, "50")), ME, false);
        assertEquals(List.of(21L), trimmed.getAllocations().stream().map(a -> a.getCreditItem().getId()).toList());
    }

    @Test
    void theDebitOfATrackedDebtCantChange() {
        Debt created = service.create(draft(10L, "100", null), ME);
        assertInvalid("can't be changed", () -> service.update(created.getId(), draft(11L, "100", null), ME, false));
    }

    @Test
    void someoneElsesDebtAnswersNotFoundUnlessAdmin() {
        Debt created = service.create(draft(10L, "100", null), ME);

        assertThrows(ItemNotFoundException.class, () -> service.get(created.getId(), SOMEONE_ELSE, false));
        assertThrows(ItemNotFoundException.class, () -> service.delete(created.getId(), SOMEONE_ELSE, false));
        assertEquals(created.getId(), service.get(created.getId(), SOMEONE_ELSE, true).getId());
    }

    @Test
    void deleteStopsTracking() {
        Debt created = service.create(draft(10L, "100", null), ME);
        service.delete(created.getId(), ME, false);

        assertTrue(service.list(ME).isEmpty());
        assertThrows(ItemNotFoundException.class, () -> service.get(created.getId(), ME, false));
    }

    // ---- in-memory repositories --------------------------------------------------------------------

    private final class InMemoryItems implements ItemRepository {
        public List<Item> findAll() { return new ArrayList<>(itemStore.values()); }
        public Optional<Item> findById(Long id) { return Optional.ofNullable(itemStore.get(id)); }
        public List<Item> findByUserIdAndItemTypeId(UUID userId, Long itemTypeId) {
            return findByUserId(userId).stream().filter(i -> i.getItemType().getId().equals(itemTypeId)).toList();
        }
        public List<Item> findByUserId(UUID userId) {
            return itemStore.values().stream().filter(i -> i.getUserId().equals(userId)).toList();
        }
        public Item save(Item entity) { itemStore.put(entity.getId(), entity); return entity; }
        public void deleteById(Long id) { itemStore.remove(id); }
    }

    private final class InMemoryDebts implements DebtRepository {
        private long nextId = 1;
        private long nextAllocationId = 1;

        public List<Debt> findByUserId(UUID userId) {
            return debtStore.values().stream().filter(d -> d.getUserId().equals(userId)).toList();
        }
        public Optional<Debt> findById(Long id) { return Optional.ofNullable(debtStore.get(id)); }
        public Debt save(Debt debt) {
            if (debt.getId() == null) {
                debt.setId(nextId++);
            }
            for (DebtAllocation a : debt.getAllocations()) {
                if (a.getId() == null) {
                    a.setId(nextAllocationId++);
                }
            }
            debtStore.put(debt.getId(), debt);
            return debt;
        }
        public void deleteById(Long id) {
            if (debtStore.remove(id) == null) {
                throw new ItemNotFoundException("Debt " + id + " not found");
            }
        }
    }
}
