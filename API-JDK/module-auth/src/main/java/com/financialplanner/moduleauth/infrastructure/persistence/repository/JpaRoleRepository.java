package com.financialplanner.moduleauth.infrastructure.persistence.repository;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    List<Role> findAllByIdIn(List<Long> ids);

    // Return only names when you only need role strings for authorities or login response
    @Query("select r.name from Role r where r.id in :ids")
    List<String> findNamesByIdIn(@Param("ids") List<Long> ids);
}
