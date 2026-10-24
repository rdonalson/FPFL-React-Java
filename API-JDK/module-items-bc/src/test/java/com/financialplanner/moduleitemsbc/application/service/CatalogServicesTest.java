package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.domain.repository.TimePeriodRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** The two admin catalogs (item types, time periods) follow the same rules. */
class CatalogServicesTest {

    @Nested
    class ItemTypes {
        private final ItemTypeRepository repo = mock(ItemTypeRepository.class);
        private final ItemTypeServiceImpl service = new ItemTypeServiceImpl(repo);
        private final ItemType credit = new ItemType(1L, "Credit");

        @Test
        void listAndGet() {
            when(repo.findAll()).thenReturn(null);
            assertEquals(List.of(), service.list());
            when(repo.findById(1L)).thenReturn(Optional.of(credit));
            assertSame(credit, service.get(1L));
            when(repo.findById(9L)).thenReturn(Optional.empty());
            assertEquals("ItemType 9 not found", assertThrows(ItemNotFoundException.class, () -> service.get(9L)).getMessage());
            assertThrows(DomainValidationException.class, () -> service.get(0L));
            assertThrows(DomainValidationException.class, () -> service.get(null));
        }

        @Test
        void createRefusesAnExistingId() {
            when(repo.findById(1L)).thenReturn(Optional.of(credit));
            assertThrows(DuplicateItemException.class, () -> service.create(new ItemType(1L, "Again")));
            verify(repo, never()).save(any());

            ItemType other = new ItemType(4L, "Other");
            when(repo.findById(4L)).thenReturn(Optional.empty());
            when(repo.save(other)).thenReturn(other);
            assertSame(other, service.create(other));
        }

        @Test
        void updateNeedsAnExistingRow() {
            ItemType renamed = new ItemType(1L, "Income");
            when(repo.findById(1L)).thenReturn(Optional.of(credit));
            when(repo.save(renamed)).thenReturn(renamed);
            assertSame(renamed, service.update(renamed));

            when(repo.findById(5L)).thenReturn(Optional.empty());
            assertThrows(ItemNotFoundException.class, () -> service.update(new ItemType(5L, "x")));
        }

        @Test
        void deleteChecksTheId() {
            service.delete(4L);
            verify(repo).deleteById(4L);
            assertThrows(DomainValidationException.class, () -> service.delete(-1L));
        }
    }

    @Nested
    class TimePeriods {
        private final TimePeriodRepository repo = mock(TimePeriodRepository.class);
        private final TimePeriodServiceImpl service = new TimePeriodServiceImpl(repo);
        private final TimePeriod monthly = new TimePeriod(6L, "Monthly");

        @Test
        void listAndGet() {
            when(repo.findAll()).thenReturn(List.of(monthly));
            assertEquals(List.of(monthly), service.list());
            when(repo.findById(6L)).thenReturn(Optional.of(monthly));
            assertSame(monthly, service.get(6L));
            when(repo.findById(11L)).thenReturn(Optional.empty());
            assertEquals("TimePeriod 11 not found", assertThrows(ItemNotFoundException.class, () -> service.get(11L)).getMessage());
            assertThrows(DomainValidationException.class, () -> service.get(0L));
        }

        @Test
        void createUpdateDelete() {
            when(repo.findById(6L)).thenReturn(Optional.of(monthly));
            assertThrows(DuplicateItemException.class, () -> service.create(new TimePeriod(6L, "Monthly")));

            TimePeriod hourly = new TimePeriod(11L, "Hourly");
            when(repo.findById(11L)).thenReturn(Optional.empty());
            when(repo.save(hourly)).thenReturn(hourly);
            assertSame(hourly, service.create(hourly));
            assertThrows(ItemNotFoundException.class, () -> service.update(hourly));

            when(repo.save(monthly)).thenReturn(monthly);
            assertSame(monthly, service.update(monthly));

            service.delete(11L);
            verify(repo).deleteById(11L);
            assertThrows(DomainValidationException.class, () -> service.delete(null));
        }
    }
}
