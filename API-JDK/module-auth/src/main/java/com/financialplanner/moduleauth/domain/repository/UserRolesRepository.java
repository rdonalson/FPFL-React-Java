package com.financialplanner.moduleauth.domain.repository;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.UserRoles;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for user -> role mappings.
 *
 * <p><b>replaceRolesForUser</b> contract:
 * - The caller MUST pass a non-null {@code roleIds} list.
 * - An empty list {@code Collections.emptyList()} means "clear all roles for the user".
 * - Passing {@code null} is not allowed and implementations should throw {@link IllegalArgumentException}.
 *
 * <p>Delete methods return the number of rows affected so callers can assert success when desired.
 */
public interface UserRolesRepository {

    List<Long> findRoleIdsByUserId(Long userId);

    List<Long> findUserIdsByRoleId(Long roleId);

    List<UserRoles> findByUserId(Long userId);

    Optional<UserRoles> findByUserIdAndRoleId(Long userId, Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    UserRoles save(UserRoles userRoles);

    /**
     * Delete a single mapping. Returns number of rows deleted (0 or 1).
     */
    int deleteByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * Delete all mappings for a user. Returns number of rows deleted.
     */
    int deleteByUserId(Long userId);

    /**
     * Replace all roles for a user atomically (delete existing then insert new).
     *
     * <p>Contract:
     * - {@code roleIds} must not be null.
     * - If {@code roleIds} is empty, the implementation MUST delete all existing mappings (i.e., clear roles).
     * - Implementations MUST perform this operation in a single transaction.
     */
    void replaceRolesForUser(Long userId, List<Long> roleIds);
}
