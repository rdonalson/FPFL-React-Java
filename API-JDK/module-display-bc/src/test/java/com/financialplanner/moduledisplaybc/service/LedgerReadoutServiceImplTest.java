package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.DebtPaydown;
import com.financialplanner.moduleitemsbc.domain.debt.DebtDraft;
import com.financialplanner.moduleitemsbc.domain.service.DebtService;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The ledger deducts Debt Paydown allocations; the paydown endpoint's service follows the same debts. */
class LedgerReadoutServiceImplTest {

    private static final UUID USER = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private static LocalDate d(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    private static Item item(long id, String name, long type, double amount, long period) {
        Item item = new Item();
        item.setId(id);
        item.setUserId(USER);
        item.setName(name);
        item.setAmount(amount);
        item.setItemType(new ItemType(type, type == 1 ? "Credit" : type == 2 ? "Debit" : "Initial Amount"));
        if (period > 0) {
            item.setTimePeriod(new TimePeriod(period, period == 6 ? "Monthly" : "Every Two Weeks"));
        }
        return item;
    }

    private final Item initial = item(1, "Initial Amount", 3, 100, 0);
    private final Item card = cardItem();
    private final Item paycheck = paycheckItem();

    private Item cardItem() {
        Item card = item(10, "Chase Visa", 2, 200, 6);
        card.setMonthlyDom(15);
        return card;
    }

    private Item paycheckItem() {
        Item paycheck = item(20, "Paycheck", 1, 2000, 4);
        paycheck.setEveryOtherWeekDow(5);
        paycheck.setBeginDate(d(10, 2));
        return paycheck;
    }

    private Debt visaDebt(String balance) {
        Debt debt = new Debt();
        debt.setId(1L);
        debt.setUserId(USER);
        debt.setItem(card);
        debt.setBalance(new BigDecimal(balance));
        debt.setApr(BigDecimal.ZERO);
        debt.setBalanceDate(d(10, 17));
        DebtAllocation allocation = new DebtAllocation();
        allocation.setId(1L);
        allocation.setDebt(debt);
        allocation.setCreditItem(paycheck);
        allocation.setPercent(new BigDecimal("25"));
        debt.getAllocations().add(allocation);
        return debt;
    }

    private LedgerReadoutServiceImpl service(List<Debt> debts) {
        List<Item> items = List.of(initial, card, paycheck);
        return new LedgerReadoutServiceImpl(new FakeItems(items), new FakeDebts(debts));
    }

    private static LedgerDto day(List<LedgerDto> ledger, LocalDate date) {
        return ledger.stream().filter(r -> r.getWDate().equals(date)).findFirst().orElseThrow();
    }

    @Test
    void allocationsAreExtraDebtPaydownDebitsOnTheLedger() {
        List<LedgerDto> ledger = service(List.of(visaDebt("1000")))
            .buildLedgerReadout(new LedgerRequest(USER, d(10, 18), d(11, 30), false));

        LedgerDto oct30 = day(ledger, d(10, 30));
        assertEquals(2000.0, oct30.getCreditSummary());
        assertEquals(-500.0, oct30.getDebitSummary());
        assertEquals(1500.0, oct30.getNet());

        ItemDto entry = oct30.getItems().getLast();
        assertEquals(LedgerReadoutService.PAYDOWN_TYPE_NAME, entry.getItemType());
        assertEquals("Chase Visa paydown", entry.getName());
        assertEquals("25% of Paycheck", entry.getPeriod());
        assertEquals(2, entry.getFkItemType());
        assertEquals(-500.0, entry.getAmount());

        // Paid off on Nov 13: no deduction on Nov 27, but the card's own payment still runs on Nov 15
        assertEquals(-500.0, day(ledger, d(11, 13)).getDebitSummary());
        assertEquals(-200.0, day(ledger, d(11, 15)).getDebitSummary());
        assertEquals(0.0, day(ledger, d(11, 27)).getDebitSummary());

        // 100 + 3 paychecks − 2 × 500 paydown − the Nov 15 card payment
        assertEquals(100 + 3 * 2000 - 1000 - 200, ledger.getLast().getRunningTotal(), 0.001);
    }

    @Test
    void withoutTrackedDebtsTheLedgerIsUnchanged() {
        List<LedgerDto> ledger = service(List.of()).buildLedgerReadout(new LedgerRequest(USER, d(10, 18), d(11, 30), false));

        assertEquals(0.0, day(ledger, d(10, 30)).getDebitSummary());
        assertTrue(ledger.stream().flatMap(r -> r.getItems().stream())
                         .noneMatch(i -> LedgerReadoutService.PAYDOWN_TYPE_NAME.equals(i.getItemType())));
        assertEquals(100 + 3 * 2000 - 200, ledger.getLast().getRunningTotal(), 0.001);
    }

    @Test
    void paydownFollowsTheSameDebtsAndWindow() {
        List<DebtPaydown> debts = service(List.of(visaDebt("1000")))
            .buildPaydown(new LedgerRequest(USER, d(10, 18), d(11, 30), false));

        assertEquals(1, debts.size());
        assertEquals(44, debts.getFirst().balances().size());
        assertEquals(d(11, 13), debts.getFirst().payoffDate());
    }

    @Test
    void paydownChecksTheRequest() {
        LedgerReadoutServiceImpl service = service(List.of());

        assertThrows(DomainValidationException.class, () -> service.buildPaydown(new LedgerRequest(null, d(10, 1), d(10, 2), false)));
        assertThrows(DomainValidationException.class, () -> service.buildPaydown(new LedgerRequest(USER, null, d(10, 2), false)));
        assertThrows(DomainValidationException.class, () -> service.buildPaydown(new LedgerRequest(USER, d(10, 2), d(10, 1), false)));
    }

    // ---- fakes ---------------------------------------------------------------------------------

    private record FakeItems(List<Item> items) implements ItemService {
        public List<Item> list() { return items; }
        public Item get(Long id) { return items.stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow(); }
        public List<Item> findByUserIdAndItemTypeId(UUID userId, Long itemTypeId) { return items; }
        public List<Item> findByUserId(UUID userId) { return items; }
        public Item create(Item entity) { throw new UnsupportedOperationException(); }
        public Item update(Long id, Item entity) { throw new UnsupportedOperationException(); }
        public void delete(Long id) { throw new UnsupportedOperationException(); }
    }

    private record FakeDebts(List<Debt> debts) implements DebtService {
        public List<Debt> list(UUID userId) { return debts; }
        public Debt get(Long id, UUID requester, boolean admin) { throw new UnsupportedOperationException(); }
        public Debt create(DebtDraft draft, UUID owner) { throw new UnsupportedOperationException(); }
        public Debt update(Long id, DebtDraft draft, UUID requester, boolean admin) { throw new UnsupportedOperationException(); }
        public void delete(Long id, UUID requester, boolean admin) { throw new UnsupportedOperationException(); }
    }
}
