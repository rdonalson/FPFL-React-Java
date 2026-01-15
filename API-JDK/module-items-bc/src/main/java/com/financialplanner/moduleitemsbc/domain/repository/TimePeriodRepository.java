package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing the repository for managing {@link TimePeriod} instances.
 * Provides CRUD operations for persisting and retrieving time period-related data
 * from the underlying data source.
 *
 * This interface defines the contract for managing TimePeriod entities. It abstracts
 * persistence details and facilitates interaction between the application and the data store.
 */
public interface TimePeriodRepository {
    TimePeriod save(TimePeriod entity);

    Optional<TimePeriod> findById(Long id);

    List<TimePeriod> findAll();

    void deleteById(Long id);
}
