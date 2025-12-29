package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.domain.entity.TimePeriodEntity;
import com.financialplanner.moduleitemsbc.domain.model.TimePeriod;
import java.util.List;
import java.util.Optional;

public interface TimePeriodRepository {
    TimePeriod save(TimePeriodEntity entity);
    Optional<TimePeriod> findById(Long id);
    List<TimePeriod> findAll();
    void deleteById(Long id);
}
