package com.financialplanner.moduleitemsbc.infrastructure.persistence.adapter;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * The adapters pass calls through to Spring Data and translate failures: a constraint violation on save is a
 * duplicate (409), any other database failure a RepositoryException (500), and deleting a missing row a 404.
 */
class RepositoryAdaptersTest {

    private static final UUID USER = UUID.randomUUID();
    private static final QueryTimeoutException DB_DOWN = new QueryTimeoutException("db down");
    private static final DataIntegrityViolationException CONSTRAINT = new DataIntegrityViolationException("dup");

    @Test
    void itemRepository() {
        JpaItemRepository jpa = mock(JpaItemRepository.class);
        ItemRepositoryImpl repo = new ItemRepositoryImpl(jpa);
        Item item = new Item();
        when(jpa.findAll()).thenReturn(List.of(item));
        when(jpa.findById(1L)).thenReturn(Optional.of(item));
        when(jpa.findByUserId(USER)).thenReturn(List.of(item));
        when(jpa.findByUserIdAndItemTypeId(USER, 2L)).thenReturn(List.of(item));
        when(jpa.save(item)).thenReturn(item);
        when(jpa.existsById(1L)).thenReturn(true);

        assertEquals(List.of(item), repo.findAll());
        assertSame(item, repo.findById(1L).orElseThrow());
        assertEquals(List.of(item), repo.findByUserId(USER));
        assertEquals(List.of(item), repo.findByUserIdAndItemTypeId(USER, 2L));
        assertSame(item, repo.save(item));
        repo.deleteById(1L);
        verify(jpa).deleteById(1L);
    }

    @Test
    void itemRepositoryTranslatesFailures() {
        JpaItemRepository jpa = mock(JpaItemRepository.class);
        ItemRepositoryImpl repo = new ItemRepositoryImpl(jpa);
        when(jpa.findAll()).thenThrow(DB_DOWN);
        when(jpa.findById(1L)).thenThrow(DB_DOWN);
        when(jpa.findByUserId(USER)).thenThrow(DB_DOWN);
        when(jpa.findByUserIdAndItemTypeId(USER, 1L)).thenThrow(DB_DOWN);
        when(jpa.save(any())).thenThrow(CONSTRAINT);
        when(jpa.existsById(9L)).thenReturn(false);
        when(jpa.existsById(3L)).thenReturn(true);
        doThrow(CONSTRAINT).when(jpa).deleteById(3L);

        assertThrows(RepositoryException.class, repo::findAll);
        assertThrows(RepositoryException.class, () -> repo.findById(1L));
        assertThrows(RepositoryException.class, () -> repo.findByUserId(USER));
        assertThrows(RepositoryException.class, () -> repo.findByUserIdAndItemTypeId(USER, 1L));
        assertThrows(DuplicateItemException.class, () -> repo.save(new Item()));
        assertThrows(ItemNotFoundException.class, () -> repo.deleteById(9L));
        assertThrows(RepositoryException.class, () -> repo.deleteById(3L)); // still referenced

        reset(jpa);
        when(jpa.save(any())).thenThrow(DB_DOWN);
        assertThrows(RepositoryException.class, () -> repo.save(new Item()));
    }

    @Test
    void itemTypeRepository() {
        JpaItemTypeRepository jpa = mock(JpaItemTypeRepository.class);
        ItemTypeRepositoryImpl repo = new ItemTypeRepositoryImpl(jpa);
        ItemType t = new ItemType(1L, "Credit");
        when(jpa.findAll()).thenReturn(List.of(t));
        when(jpa.findById(1L)).thenReturn(Optional.of(t));
        when(jpa.getReferenceById(1L)).thenReturn(t);
        when(jpa.save(t)).thenReturn(t);
        when(jpa.existsById(1L)).thenReturn(true);

        assertEquals(List.of(t), repo.findAll());
        assertSame(t, repo.findById(1L).orElseThrow());
        assertSame(t, repo.getReferenceById(1L));
        assertSame(t, repo.save(t));
        repo.deleteById(1L);
        verify(jpa).deleteById(1L);

        reset(jpa);
        when(jpa.findAll()).thenThrow(DB_DOWN);
        when(jpa.findById(1L)).thenThrow(DB_DOWN);
        when(jpa.getReferenceById(1L)).thenThrow(DB_DOWN);
        when(jpa.save(t)).thenThrow(CONSTRAINT);
        when(jpa.existsById(2L)).thenReturn(false);
        assertThrows(RepositoryException.class, repo::findAll);
        assertThrows(RepositoryException.class, () -> repo.findById(1L));
        assertThrows(RepositoryException.class, () -> repo.getReferenceById(1L));
        assertThrows(DuplicateItemException.class, () -> repo.save(t));
        assertThrows(ItemNotFoundException.class, () -> repo.deleteById(2L));
    }

    @Test
    void timePeriodRepository() {
        JpaTimePeriodRepository jpa = mock(JpaTimePeriodRepository.class);
        TimePeriodRepositoryImpl repo = new TimePeriodRepositoryImpl(jpa);
        TimePeriod p = new TimePeriod(6L, "Monthly");
        when(jpa.findAll()).thenReturn(List.of(p));
        when(jpa.findById(6L)).thenReturn(Optional.of(p));
        when(jpa.getReferenceById(6L)).thenReturn(p);
        when(jpa.save(p)).thenReturn(p);
        when(jpa.existsById(6L)).thenReturn(true);

        assertEquals(List.of(p), repo.findAll());
        assertSame(p, repo.findById(6L).orElseThrow());
        assertSame(p, repo.getReferenceById(6L));
        assertSame(p, repo.save(p));
        doThrow(CONSTRAINT).when(jpa).deleteById(6L);
        assertThrows(RepositoryException.class, () -> repo.deleteById(6L)); // items still use it

        reset(jpa);
        when(jpa.findAll()).thenThrow(DB_DOWN);
        when(jpa.save(p)).thenThrow(DB_DOWN);
        when(jpa.existsById(99L)).thenReturn(false);
        assertThrows(RepositoryException.class, repo::findAll);
        assertThrows(RepositoryException.class, () -> repo.save(p));
        assertThrows(ItemNotFoundException.class, () -> repo.deleteById(99L));
    }

    @Test
    void debtRepository() {
        JpaDebtRepository jpa = mock(JpaDebtRepository.class);
        DebtRepositoryImpl repo = new DebtRepositoryImpl(jpa);
        Debt debt = new Debt();
        when(jpa.findByUserIdOrderByIdAsc(USER)).thenReturn(List.of(debt));
        when(jpa.findById(1L)).thenReturn(Optional.of(debt));
        when(jpa.saveAndFlush(debt)).thenReturn(debt);
        when(jpa.existsById(1L)).thenReturn(true);

        assertEquals(List.of(debt), repo.findByUserId(USER));
        assertSame(debt, repo.findById(1L).orElseThrow());
        assertSame(debt, repo.save(debt));
        repo.deleteById(1L);
        verify(jpa).deleteById(1L);

        reset(jpa);
        when(jpa.findByUserIdOrderByIdAsc(USER)).thenThrow(DB_DOWN);
        when(jpa.findById(1L)).thenThrow(DB_DOWN);
        when(jpa.saveAndFlush(debt)).thenThrow(CONSTRAINT);
        when(jpa.existsById(2L)).thenReturn(false);
        assertThrows(RepositoryException.class, () -> repo.findByUserId(USER));
        assertThrows(RepositoryException.class, () -> repo.findById(1L));
        assertEquals("That debit is already being tracked",
                     assertThrows(DuplicateItemException.class, () -> repo.save(debt)).getMessage());
        assertThrows(ItemNotFoundException.class, () -> repo.deleteById(2L));
    }
}
