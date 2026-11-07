package com.financialplanner.moduleitemsbc.infrastructure.persistence.adapter;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.domain.repository.CreditAccountRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.CreditAccount;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.entity.JpaCreditAccountRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link CreditAccountRepository} over Spring Data, translating data-access failures like {@link DebtRepositoryImpl}. */
@Component
public class CreditAccountRepositoryImpl implements CreditAccountRepository {

    private final JpaCreditAccountRepository jpa;

    public CreditAccountRepositoryImpl(JpaCreditAccountRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<CreditAccount> findByUserId(UUID userId) {
        try {
            return jpa.findByUserIdOrderByNameAscIdAsc(userId);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching accounts for user " + userId, ex);
        }
    }

    @Override
    public Optional<CreditAccount> findById(Long id) {
        try {
            return jpa.findById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching account " + id, ex);
        }
    }

    @Override
    public CreditAccount save(CreditAccount account) {
        try {
            return jpa.saveAndFlush(account);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateItemException("You already have an account named \"" + account.getName() + "\"", ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while saving account " + account.getId(), ex);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            if (!jpa.existsById(id)) {
                throw new ItemNotFoundException("Account " + id + " not found");
            }
            jpa.deleteById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while deleting account " + id, ex);
        }
    }
}
