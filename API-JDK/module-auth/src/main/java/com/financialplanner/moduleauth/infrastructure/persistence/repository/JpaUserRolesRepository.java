package com.financialplanner.moduleauth.infrastructure.persistence.repository;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.UserRoles;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.UserRolesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface JpaUserRolesRepository extends JpaRepository<UserRoles, UserRolesId> {

    List<UserRoles> findByUserId(Long userId);
    List<UserRoles> findByRoleId(Long roleId);
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    @Query("select ur.roleId from UserRoles ur where ur.userId = :userId")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    @Query("select ur.userId from UserRoles ur where ur.roleId = :roleId")
    List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);

    Optional<UserRoles> findByUserIdAndRoleId(Long userId, Long roleId);

    @Modifying
    @Transactional
    @Query("delete from UserRoles ur where ur.userId = :userId and ur.roleId = :roleId")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Modifying
    @Transactional
    @Query("delete from UserRoles ur where ur.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
