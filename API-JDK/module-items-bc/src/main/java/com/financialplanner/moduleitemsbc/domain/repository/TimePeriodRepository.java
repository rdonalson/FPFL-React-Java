package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing the repository for managing {@code TimePeriod} entities.
 * Provides CRUD (Create, Read, Update, Delete) operations for persisting and retrieving
 * time period-related data from the underlying data source.
 *
 * This interface serves as a contract for interacting with {@code TimePeriod} entities
 * and abstracts the persistence layer, enabling consistent data operations within the application.
 */
public interface TimePeriodRepository {
    TimePeriod save(TimePeriod entity);

    Optional<TimePeriod> findById(Long id);

    List<TimePeriod> findAll();

    void deleteById(Long id);
}
