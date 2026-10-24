package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountRequest;
import com.financialplanner.moduleapi.dtos.item.ItemRequest;
import com.financialplanner.moduleapi.dtos.item.ItemResponse;
import com.financialplanner.moduleapi.dtos.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodRequest;
import com.financialplanner.modulecommonbc.exception.SanitizationException;
import com.financialplanner.modulecommonbc.sanitizer.SanitizerImpl;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.domain.repository.TimePeriodRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogMappersTest {

    private static final UUID USER = UUID.randomUUID();
    private final SanitizerImpl sanitizer = new SanitizerImpl();
    private final ItemTypeRepository itemTypes = mock(ItemTypeRepository.class);
    private final TimePeriodRepository periods = mock(TimePeriodRepository.class);

    @Test
    void itemRequestBecomesAnEntityWithItsTypeAndPeriod() {
        when(itemTypes.getReferenceById(2L)).thenReturn(new ItemType(2L, "Debit"));
        when(periods.findById(6L)).thenReturn(Optional.of(new TimePeriod(6L, "Monthly")));
        ItemRequest req = new ItemRequest(USER, "Rent", 1600.0, 2, 6, null, null, null, null, null, null, 1, null, null, null,
                                          null, null, null, null, null, null, null, null, null, null, null, null, null, false);

        Item item = new ItemMapper(sanitizer, itemTypes, periods).toEntity(req);

        assertNull(item.getId());
        assertEquals("Rent", item.getName());
        assertEquals(2L, item.getItemType().getId());
        assertEquals("Monthly", item.getTimePeriod().getName());
        assertEquals(1, item.getMonthlyDom());
    }

    @Test
    void anUnknownPeriodIsLeftEmpty() {
        when(periods.findById(9L)).thenReturn(Optional.empty());
        ItemRequest req = new ItemRequest(USER, "Gift", 50.0, 1, 9, null, null, null, null, null, null, null, null, null,
                                          null, null, null, null, null, null, null, null, null, null, 12, 25, null, null,
                                          false);
        assertNull(new ItemMapper(sanitizer, itemTypes, periods).toEntity(req).getTimePeriod());
    }

    @Test
    void itemResponseCopiesTheEntity() {
        Item item = new Item(5L, USER, "Rent", 1600.0, new ItemType(2L, "Debit"), new TimePeriod(6L, "Monthly"),
                             null, null, null, null, null, null, 1, null, null, null, null, null, null, null, null,
                             null, null, null, null, null, null, null, null, false);
        ItemResponse res = new ItemMapper(sanitizer, itemTypes, periods).toResponse(item);
        assertEquals(5L, res.id());
        assertEquals(1, res.monthlyDom());
        assertEquals("Monthly", res.TimePeriod().getName());

        item.setName("Rent & stuff");
        assertThrows(SanitizationException.class, () -> new ItemMapper(sanitizer, itemTypes, periods).toResponse(item));
    }

    @Test
    void initialAmountIsTypeThreeNamedIaDatedToday() {
        when(itemTypes.getReferenceById(3L)).thenReturn(new ItemType(3L, "Initial Amount"));
        Item item = new InitialAmountMapper(sanitizer, itemTypes).toEntity(new InitialAmountRequest(USER, 250.0));

        assertEquals("IA", item.getName());
        assertEquals(3L, item.getItemType().getId());
        assertEquals(LocalDate.now(), item.getBeginDate());
        assertEquals(250.0, item.getAmount());
        assertEquals(250.0, new InitialAmountMapper(sanitizer, itemTypes).toResponse(item).amount());
    }

    @Test
    void itemTypeAndTimePeriodMappers() {
        ItemTypeMapper itm = new ItemTypeMapper(sanitizer);
        assertEquals(new ItemType(4L, "Other"), itm.toEntity(new ItemTypeRequest(4L, "Other")));
        // Request records are checked (bad characters are refused) but not rewritten
        assertThrows(SanitizationException.class, () -> itm.toEntity(new ItemTypeRequest(4L, "<b>Other</b>")));
        assertEquals("Other", itm.toResponse(new ItemType(4L, "Other")).name());

        TimePeriodMapper tpm = new TimePeriodMapper(sanitizer);
        assertEquals(new TimePeriod(11L, "Hourly"), tpm.toEntity(new TimePeriodRequest(11L, "Hourly")));
        assertEquals(11L, tpm.toResponse(new TimePeriod(11L, "Hourly")).id());
    }
}
