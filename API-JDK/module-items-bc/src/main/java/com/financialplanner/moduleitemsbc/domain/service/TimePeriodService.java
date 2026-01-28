package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;

import java.util.List;

/**
 * Service interface for managing {@code TimePeriod} entities.
 * This interface defines the operations for creating, retrieving, updating,
 * deleting, and listing {@code TimePeriod} entities. It acts as an abstraction
 * for the business logic to interact with the data persistence layer.
 * Responsibilities:
 * - Providing a contract for operations on {@code TimePeriod}.
 * - Enforcing application-level constraints for data integrity.
 * - Ensuring validation of inputs for all operations.
 * Methods:
 * - {@code list()}: Retrieves a list of all {@code TimePeriod} entities.
 * - {@code get(Long id)}: Retrieves a specific {@code TimePeriod} entity by its identifier.
 * - {@code create(TimePeriod entity)}: Creates a new {@code TimePeriod} entity in the system.
 * - {@code update(TimePeriod entity)}: Updates the details of an existing {@code TimePeriod}.
 * - {@code delete(Long id)}: Deletes a specific {@code TimePeriod} entity by its identifier.
 * Exceptions handled by implementations (not included in the interface):
 * - Validation exceptions for missing or invalid inputs.
 * - Exceptions when entities cannot be found.
 * - Exceptions for duplicate entities during creation.
 */
public interface TimePeriodService {
    List<TimePeriod> list();
    TimePeriod get(Long id);
    TimePeriod create(TimePeriod entity);
    TimePeriod update(TimePeriod entity);
    void delete(Long id);
}
