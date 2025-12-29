package com.financialplanner.moduleitemsbc.infrastructure.repository;

import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaItemTypeRepository extends JpaRepository<ItemTypeEntity, Long> {}

