package com.financialplanner.moduleauth.domain.service;

import java.util.List;

public interface UserRolesService {

    /**
     * Returns role IDs assigned to the user.
     */
    List<Long> getRoleIdsForUser(Long userId);

    /**
     * Returns role names assigned to the user.
     * Useful for login, JWT claims, and GrantedAuthority mapping.
     */
    List<String> getRoleNamesForUser(Long userId);

    /**
     * Assign a single role to a user.
     */
    void addRoleToUser(Long userId, Long roleId);

    /**
     * Remove a single role from a user.
     */
    int removeRoleFromUser(Long userId, Long roleId);

    /**
     * Replace all roles for a user atomically.
     * Passing an empty list clears all roles.
     */
    void replaceRolesForUser(Long userId, List<Long> roleIds);

    /**
     * Check if user has a specific role.
     */
    boolean userHasRole(Long userId, Long roleId);
}

