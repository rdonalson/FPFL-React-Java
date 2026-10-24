package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.mapper.ItemEntityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    private static final UUID USER = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private final ItemRepository repo = mock(ItemRepository.class);
    private final ItemServiceImpl service = new ItemServiceImpl(repo, new ItemEntityMapper());

    private static Item item(Long id, String name) {
        Item item = new Item();
        item.setId(id);
        item.setUserId(USER);
        item.setName(name);
        item.setAmount(100.0);
        return item;
    }

    @Test
    void listNeverReturnsNull() {
        when(repo.findAll()).thenReturn(null);
        assertEquals(List.of(), service.list());

        when(repo.findAll()).thenReturn(List.of(item(1L, "Rent")));
        assertEquals(1, service.list().size());
    }

    @Test
    void getFindsAnItemOrSaysItIsMissing() {
        when(repo.findById(1L)).thenReturn(Optional.of(item(1L, "Rent")));
        when(repo.findById(2L)).thenReturn(Optional.empty());

        assertEquals("Rent", service.get(1L).getName());
        var ex = assertThrows(ItemNotFoundException.class, () -> service.get(2L));
        assertEquals("Item 2 not found", ex.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1})
    void idsMustBePositive(Long id) {
        assertThrows(DomainValidationException.class, () -> service.get(id));
        assertThrows(DomainValidationException.class, () -> service.update(id, item(null, "x")));
        assertThrows(DomainValidationException.class, () -> service.delete(id));
        verifyNoInteractions(repo);
    }

    @Test
    void findByUserNeedsAUserAndNeverReturnsNull() {
        assertThrows(DomainValidationException.class, () -> service.findByUserId(null));
        when(repo.findByUserId(USER)).thenReturn(null);
        assertEquals(List.of(), service.findByUserId(USER));
    }

    @Test
    void findByUserAndTypeChecksBoth() {
        assertThrows(DomainValidationException.class, () -> service.findByUserIdAndItemTypeId(null, 1L));
        assertThrows(DomainValidationException.class, () -> service.findByUserIdAndItemTypeId(USER, 0L));
        when(repo.findByUserIdAndItemTypeId(USER, 2L)).thenReturn(List.of(item(1L, "Rent")));
        assertEquals(1, service.findByUserIdAndItemTypeId(USER, 2L).size());
    }

    @Test
    void createSaves() {
        Item rent = item(null, "Rent");
        when(repo.save(rent)).thenReturn(item(5L, "Rent"));
        assertEquals(5L, service.create(rent).getId());
    }

    @Test
    void updateSavesACopyUnderThePathId() {
        when(repo.findById(5L)).thenReturn(Optional.of(item(5L, "Old")));
        when(repo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item incoming = item(999L, "New name"); // the body's id is ignored
        Item saved = service.update(5L, incoming);

        assertEquals(5L, saved.getId());
        assertEquals("New name", saved.getName());
        assertNotSame(incoming, saved);
    }

    @Test
    void updatingAMissingItemFails() {
        when(repo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> service.update(5L, item(null, "x")));
        verify(repo, never()).save(any());
    }

    @Test
    void deleteDelegates() {
        service.delete(5L);
        verify(repo).deleteById(5L);
    }
}
