package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;

import java.util.List;
import java.util.Optional;

public interface TimePeriodRepository {
    List<TimePeriod> findAll();
    Optional<TimePeriod> findById(Long id);
    TimePeriod getReferenceById(Long id);
    TimePeriod save(TimePeriod entity);
    void deleteById(Long id);
}
