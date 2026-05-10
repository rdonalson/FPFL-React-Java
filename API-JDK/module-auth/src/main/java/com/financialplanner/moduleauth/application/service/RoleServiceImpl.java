package com.financialplanner.moduleauth.application.service;

import com.financialplanner.moduleauth.domain.repository.RoleRepository;
import com.financialplanner.moduleauth.domain.service.RoleService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    @Transactional
    public Role createRole(String name) {
        // Normalize name (optional)
        String normalized = name.trim().toUpperCase();

        // Prevent duplicates at service level
        return roleRepository.findByName(normalized)
                             .orElseGet(() -> roleRepository.save(new Role(normalized)));
    }

    @Override
    @Transactional
    public Role ensureRoleExists(String name) {
        return findByName(name).orElseGet(() -> createRole(name));
    }
}
