package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing the repository for managing {@link TimePeriod} instances.
 * Provides CRUD operations for persisting and retrieving time period-related data
 * from the underlying data source.
 *
 * This interface defines the contract for managing TimePeriod entities and transforms
 * them into domain models. It facilitates a separation between business logic
 * and persistence operations.
 */
public interface TimePeriodRepository {
    com.financialplanner.moduleitemsbc.domain.model.TimePeriod save(TimePeriod entity);

    Optional<com.financialplanner.moduleitemsbc.domain.model.TimePeriod> findById(Long id);

    List<com.financialplanner.moduleitemsbc.domain.model.TimePeriod> findAll();

    void deleteById(Long id);
}
