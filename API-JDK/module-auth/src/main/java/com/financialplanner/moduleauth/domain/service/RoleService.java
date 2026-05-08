package com.financialplanner.moduleauth.domain.service;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> findByName(String name);
    Role createRole(String name);
    Role ensureRoleExists(String name);
}
