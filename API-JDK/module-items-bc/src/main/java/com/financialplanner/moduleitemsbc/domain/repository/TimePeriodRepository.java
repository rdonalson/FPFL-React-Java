package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;

import java.util.List;
import java.util.Optional;

/**
 * Provides an abstraction for managing {@code TimePeriod} entities within the repository layer.
 * This interface defines standard CRUD (Create, Read, Update, Delete) operations and serves
 * as a contract for interacting with the underlying data store that contains {@code TimePeriod} data.
 * Responsibilities:
 * - Retrieve all {@code TimePeriod} entities.
 * - Retrieve a specific {@code TimePeriod} entity by its ID.
 * - Save a new or updated {@code TimePeriod} entity.
 * - Delete a {@code TimePeriod} entity by its ID.
 * Usage:
 * - This interface is used within service layers or other components that require access
 * to {@code TimePeriod} data.
 * Thread Safety:
 * - Implementation of this interface should ensure thread safety if used in a multithreaded environment.
 */
public interface TimePeriodRepository {
    List<TimePeriod> findAll();

    Optional<TimePeriod> findById(Long id);

    TimePeriod getReferenceById(Long id);

    TimePeriod save(TimePeriod entity);

    void deleteById(Long id);
}
