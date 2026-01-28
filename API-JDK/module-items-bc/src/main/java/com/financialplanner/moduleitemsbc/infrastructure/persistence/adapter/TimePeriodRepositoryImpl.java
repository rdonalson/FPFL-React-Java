package com.financialplanner.moduleitemsbc.infrastructure.persistence.adapter;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.domain.repository.TimePeriodRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.JpaTimePeriodRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@code TimePeriodRepository} interface that provides
 * methods for managing {@code TimePeriod} entities. This class acts as a bridge
 * between the domain logic and the underlying JPA-based persistence layer.
 * Responsibilities:
 * - Delegates CRUD operations to the {@code JpaTimePeriodRepository}.
 * - Wraps operations with error handling to manage exceptions and translate them
 * into domain-specific exceptions such as {@code RepositoryException},
 * {@code DuplicateItemException}, and {@code ItemNotFoundException}.
 * Dependencies:
 * - {@code JpaTimePeriodRepository}: Provides access to underlying persistence logic.
 * Methods:
 * - {@code findAll()}: Retrieves all {@code TimePeriod} entities from the database.
 * - {@code findById(Long id)}: Finds a {@code TimePeriod} by its unique ID.
 * - {@code getReferenceById(Long id)}: Retrieves a reference to a {@code TimePeriod}
 * entity by its ID without fully loading it.
 * - {@code save(TimePeriod timePeriod)}: Persists a new or updated {@code TimePeriod}
 * entity to the database.
 * - {@code deleteById(Long id)}: Deletes a {@code TimePeriod} entity by its unique ID.
 * Error Handling:
 * - Translates database-specific exceptions (e.g., {@code DataAccessException},
 * {@code DataIntegrityViolationException}) into domain-specific exceptions.
 * - Ensures consistent and meaningful exception reporting across repository operations.
 */
@Component
public class TimePeriodRepositoryImpl implements TimePeriodRepository {

    private final JpaTimePeriodRepository jpa;

    public TimePeriodRepositoryImpl(JpaTimePeriodRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<TimePeriod> findAll() {
        try {
            return jpa.findAll();
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching TimePeriods", ex);
        }
    }

    @Override
    public Optional<TimePeriod> findById(Long id) {
        try {
            return jpa.findById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching TimePeriod " + id, ex);
        }
    }

    public TimePeriod getReferenceById(Long id) {
        try {
            return jpa.getReferenceById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching TimePeriod " + id, ex);
        }
    }

    @Override
    public TimePeriod save(TimePeriod timePeriod) {
        try {
            return jpa.save(timePeriod);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateItemException("TimePeriod already exists: " + timePeriod.getId(), ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while saving TimePeriod " + timePeriod.getId(), ex);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            if (!jpa.existsById(id)) {
                throw new ItemNotFoundException("TimePeriod " + id + " not found");
            }
            jpa.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RepositoryException("Constraint violation while deleting TimePeriod " + id, ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while deleting TimePeriod " + id, ex);
        }
    }
}

