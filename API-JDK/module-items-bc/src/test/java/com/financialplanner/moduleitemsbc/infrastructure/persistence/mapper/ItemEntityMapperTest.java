package com.financialplanner.moduleitemsbc.infrastructure.persistence.mapper;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ItemEntityMapperTest {

    @Test
    void copiesEveryFieldUnderTheGivenId() {
        Item source = new Item(99L, UUID.randomUUID(), "Paycheck", 2000.0, new ItemType(1L, "Credit"),
                               new TimePeriod(7L, "Quarterly"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                               1, 2, 3, 4, 5, 1, 15, 4, 15, 7, 15, 10, 15, 1, 1, 7, 1, 4, 15, 2, 3, true);

        Item copy = new ItemEntityMapper().copyEntity(5L, source);

        assertEquals(5L, copy.getId());
        assertNotSame(source, copy);
        // everything but the id matches
        source.setId(5L);
        assertEquals(source, copy);
    }
}
