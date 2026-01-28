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

@Component
public class TimePeriodRepositoryImpl implements TimePeriodRepository {

    private final JpaTimePeriodRepository jpa;

    // Constructor
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

