package com.financialplanner.moduledisplaybc.model;

import com.financialplanner.modulecommonbc.sanitizer.SanitizerImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** The ledger's Cards & Loans entries pass the response sanitizer (as API-NET's AccountResponseSanitizerTests). */
class ItemDtoSanitizerTest {

    @Test
    void aCardOrLoanPaymentEntryPassesTheSanitizer() {
        ItemDto dto = new ItemDto();
        dto.setFkItemType(2);
        dto.setItemType("Card/Loan Payment");
        dto.setName("AT&T Visa (personal) payment");
        dto.setPeriod("25% of Paycheck");
        dto.setAmount(-500.0);
        dto.setOccurrenceDate("2026-10-30");

        assertDoesNotThrow(() -> new SanitizerImpl().sanitize(dto));
        assertEquals("Card/Loan Payment", dto.getItemType());
        assertEquals("AT&T Visa (personal) payment", dto.getName());
    }
}
