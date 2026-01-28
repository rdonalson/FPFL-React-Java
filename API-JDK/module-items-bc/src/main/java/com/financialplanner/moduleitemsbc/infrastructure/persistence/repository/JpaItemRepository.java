package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaItemRepository extends JpaRepository<Item, Long> {}
