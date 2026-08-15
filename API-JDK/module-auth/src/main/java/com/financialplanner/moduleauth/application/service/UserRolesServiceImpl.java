package com.financialplanner.moduleauth.application.service;

import com.financialplanner.moduleauth.domain.repository.RoleRepository;
import com.financialplanner.moduleauth.domain.repository.UserRolesRepository;
import com.financialplanner.moduleauth.domain.service.UserRolesService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.UserRoles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRolesServiceImpl implements UserRolesService {

    private final UserRolesRepository userRolesRepository;
    private final RoleRepository roleRepository;

    public UserRolesServiceImpl(UserRolesRepository userRolesRepository,
                                RoleRepository roleRepository) {
        this.userRolesRepository = userRolesRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getRoleIdsForUser(Long userId) {
        return userRolesRepository.findRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRoleNamesForUser(Long userId) {
        List<Long> roleIds = userRolesRepository.findRoleIdsByUserId(userId);

        if (roleIds.isEmpty()) {
            return List.of();
        }

        // Optimized single-query lookup
        return roleRepository.findNamesByIdIn(roleIds);
    }

    @Override
    @Transactional
    public void addRoleToUser(Long userId, Long roleId) {
        if (!userRolesRepository.existsByUserIdAndRoleId(userId, roleId)) {
            userRolesRepository.save(new UserRoles(userId, roleId));
        }
    }

    @Override
    @Transactional
    public int removeRoleFromUser(Long userId, Long roleId) {
        return userRolesRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    @Transactional
    public void replaceRolesForUser(Long userId, List<Long> roleIds) {
        userRolesRepository.replaceRolesForUser(userId, roleIds);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasRole(Long userId, Long roleId) {
        return userRolesRepository.existsByUserIdAndRoleId(userId, roleId);
    }
}
