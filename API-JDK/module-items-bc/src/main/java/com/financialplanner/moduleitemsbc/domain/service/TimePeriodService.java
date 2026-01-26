package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;

import java.util.List;

public interface TimePeriodService {
    List<TimePeriod> list();
    TimePeriod get(Long id);
    TimePeriod create(TimePeriod entity);
    TimePeriod update(TimePeriod entity);
    void delete(Long id);
}
