package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@code TimePeriod} entities.
 * This interface extends {@code JpaRepository} to provide CRUD operations
 * and additional query methods for interacting with the underlying database.
 * The {@code TimePeriod} entity represents a specific time period stored in the database
 * and is associated with the "time_periods" table in the "fpfl" schema.
 * By extending {@code JpaRepository}, this interface gains access to several
 * built-in methods such as save, findById, findAll, and deleteById. These methods
 * facilitate efficient data management and interaction with persistent storage.
 */
@Repository
public interface JpaTimePeriodRepository extends JpaRepository<TimePeriod, Long> {}
