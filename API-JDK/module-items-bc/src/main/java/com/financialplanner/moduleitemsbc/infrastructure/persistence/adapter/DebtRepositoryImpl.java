package com.financialplanner.moduleitemsbc.infrastructure.persistence.adapter;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.domain.repository.DebtRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.entity.JpaDebtRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link DebtRepository} over Spring Data, translating data-access failures like {@link ItemRepositoryImpl}. */
@Component
public class DebtRepositoryImpl implements DebtRepository {

    private final JpaDebtRepository jpa;

    public DebtRepositoryImpl(JpaDebtRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Debt> findByUserId(UUID userId) {
        try {
            return jpa.findByUserIdOrderByIdAsc(userId);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching debts for user " + userId, ex);
        }
    }

    @Override
    public Optional<Debt> findById(Long id) {
        try {
            return jpa.findById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching debt " + id, ex);
        }
    }

    @Override
    public Debt save(Debt debt) {
        try {
            return jpa.saveAndFlush(debt);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateItemException("That debit is already being tracked", ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while saving debt " + debt.getId(), ex);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            if (!jpa.existsById(id)) {
                throw new ItemNotFoundException("Debt " + id + " not found");
            }
            jpa.deleteById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while deleting debt " + id, ex);
        }
    }
}
