package com.financialplanner.moduleauth.infrastructure.persistence.adapter;

import com.financialplanner.moduleauth.domain.repository.RoleRepository;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;
import com.financialplanner.moduleauth.infrastructure.persistence.repository.JpaRoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RoleRepositoryImpl implements RoleRepository {

    private final JpaRoleRepository jpa;

    public RoleRepositoryImpl(JpaRoleRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Role> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<String> findNamesByIdIn(List<Long> ids) {
        return jpa.findNamesByIdIn(ids);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpa.findByName(name);
    }

    @Override
    public Role save(Role role) {
        return jpa.save(role);
    }
}
