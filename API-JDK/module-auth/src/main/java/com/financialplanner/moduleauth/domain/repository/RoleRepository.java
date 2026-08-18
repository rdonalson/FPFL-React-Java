package com.financialplanner.moduleauth.domain.repository;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);
    Optional<Role> findById(Long id);   // ← ADD THIS
    List<String> findNamesByIdIn(List<Long> ids);
    Role save(Role role);
}
