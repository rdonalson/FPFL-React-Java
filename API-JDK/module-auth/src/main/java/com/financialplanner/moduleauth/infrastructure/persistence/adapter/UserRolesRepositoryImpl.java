package com.financialplanner.moduleauth.infrastructure.persistence.adapter;

import com.financialplanner.moduleauth.domain.repository.UserRolesRepository;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.UserRoles;
import com.financialplanner.moduleauth.infrastructure.persistence.repository.custom.JpaUserRolesCustomRepository;
import com.financialplanner.moduleauth.infrastructure.persistence.repository.JpaUserRolesRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class UserRolesRepositoryImpl implements UserRolesRepository {

    private final JpaUserRolesRepository jpa;
    private final JpaUserRolesCustomRepository jpaCustom;

    public UserRolesRepositoryImpl(JpaUserRolesRepository jpa,
                                   JpaUserRolesCustomRepository jpaCustom) {
        this.jpa = jpa;
        this.jpaCustom = jpaCustom;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findRoleIdsByUserId(Long userId) {
        return jpa.findRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findUserIdsByRoleId(Long roleId) {
        return jpa.findUserIdsByRoleId(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoles> findByUserId(Long userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserRoles> findByUserIdAndRoleId(Long userId, Long roleId) {
        return jpa.findByUserIdAndRoleId(userId, roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndRoleId(Long userId, Long roleId) {
        return jpa.existsByUserIdAndRoleId(userId, roleId);
    }

    @Override
    @Transactional
    public UserRoles save(UserRoles userRoles) {
        return jpa.save(userRoles);
    }

    @Override
    @Transactional
    public int deleteByUserIdAndRoleId(Long userId, Long roleId) {
        return jpa.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    @Transactional
    public int deleteByUserId(Long userId) {
        return jpa.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void replaceRolesForUser(Long userId, List<Long> roleIds) {
        // jpaCustom will validate roleIds != null and perform delete+insert in a single transaction
        jpaCustom.replaceRolesForUser(userId, roleIds);
    }
}
