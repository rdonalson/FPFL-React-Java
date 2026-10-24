package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.custom;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** The hand-written queries filter on the entity's capitalized field names (UserId, ItemType.Id). */
@SuppressWarnings("unchecked")
class JpaItemRepositoryCustomImplTest {

    private static final UUID USER = UUID.randomUUID();

    private final EntityManager em = mock(EntityManager.class);
    private final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    private final CriteriaQuery<Item> cq = mock(CriteriaQuery.class);
    private final Root<Item> root = mock(Root.class);
    private final Path<Object> userPath = mock(Path.class);
    private final Path<Object> typePath = mock(Path.class);
    private final Path<Object> typeIdPath = mock(Path.class);
    private final TypedQuery<Item> query = mock(TypedQuery.class);
    private final List<Item> rows = List.of(new Item());

    @BeforeEach
    void setUp() {
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Item.class)).thenReturn(cq);
        when(cq.from(Item.class)).thenReturn(root);
        when(root.get("UserId")).thenReturn(userPath);
        when(root.get("ItemType")).thenReturn(typePath);
        when(typePath.get("Id")).thenReturn(typeIdPath);
        when(em.createQuery(cq)).thenReturn(query);
        when(query.getResultList()).thenReturn(rows);
    }

    @Test
    void findByUserIdFiltersOnTheOwner() {
        assertSame(rows, new JpaItemRepositoryCustomImpl(em).findByUserId(USER));
        verify(cb).equal(userPath, USER);
    }

    @Test
    void findByUserAndTypeAlsoFiltersOnTheItemType() {
        assertSame(rows, new JpaItemRepositoryCustomImpl(em).findByUserIdAndItemTypeId(USER, 2L));
        verify(cb).equal(userPath, USER);
        verify(cb).equal(typeIdPath, 2L);
    }
}
