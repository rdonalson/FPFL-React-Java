package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.custom;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link JpaItemRepositoryCustom} interface providing
 * custom database query operations for {@link Item} entities.
 * This class leverages the {@link EntityManager} to execute criteria-based queries
 * for retrieving items based on complex filtering conditions that are not natively
 * supported by Spring Data JPA repository methods.
 */
@Component
public class JpaItemRepositoryCustomImpl implements JpaItemRepositoryCustom {

    private final EntityManager entityManager;

    public JpaItemRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Retrieves a list of items that match the specified user ID and item type ID criteria.
     *
     * @param userId     the unique identifier of the user whose items are to be retrieved
     * @param itemTypeId the unique identifier of the item type to filter the items by
     *
     * @return a list of items that belong to the specified user and correspond to the specified item type
     */
    //@Override
    public List<Item> findByUserIdAndItemTypeId(UUID userId, Long itemTypeId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Item> cq = cb.createQuery(Item.class);
        Root<Item> root = cq.from(Item.class);

        cq.where(cb.equal(root.get("UserId"), userId), cb.equal(root.get("ItemType")
                                                                    .get("Id"), itemTypeId));

        return entityManager.createQuery(cq)
                            .getResultList();
    }
}
