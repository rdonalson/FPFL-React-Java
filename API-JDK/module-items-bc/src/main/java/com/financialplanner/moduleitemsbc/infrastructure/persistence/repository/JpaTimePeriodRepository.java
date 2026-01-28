package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTimePeriodRepository extends JpaRepository<TimePeriod, Long> {}
