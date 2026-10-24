package com.financialplanner.moduleauth.application.service;

import com.financialplanner.moduleauth.domain.repository.RoleRepository;
import com.financialplanner.moduleauth.domain.repository.UserRolesRepository;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.UserRoles;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleAndUserRolesServiceTest {

    private final RoleRepository roleRepo = mock(RoleRepository.class);
    private final UserRolesRepository userRolesRepo = mock(UserRolesRepository.class);
    private final RoleServiceImpl roles = new RoleServiceImpl(roleRepo);
    private final UserRolesServiceImpl userRoles = new UserRolesServiceImpl(userRolesRepo, roleRepo);

    // ---- RoleServiceImpl ----------------------------------------------------------------------------

    @Test
    void createRoleNormalizesTheNameAndReusesAnExistingRole() {
        Role existing = new Role("ROLE_ADMIN");
        when(roleRepo.findByName("ROLE_ADMIN")).thenReturn(Optional.of(existing));

        assertSame(existing, roles.createRole("  role_admin "));
        verify(roleRepo, never()).save(any());
    }

    @Test
    void createRoleSavesANewOne() {
        when(roleRepo.findByName("ROLE_AUDITOR")).thenReturn(Optional.empty());
        when(roleRepo.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("ROLE_AUDITOR", roles.createRole("role_auditor").getName());
    }

    @Test
    void ensureRoleExistsCreatesItOnlyWhenMissing() {
        Role user = new Role("ROLE_USER");
        when(roleRepo.findByName("ROLE_USER")).thenReturn(Optional.of(user));
        assertSame(user, roles.ensureRoleExists("ROLE_USER"));

        when(roleRepo.findByName("ROLE_NEW")).thenReturn(Optional.empty());
        when(roleRepo.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals("ROLE_NEW", roles.ensureRoleExists("ROLE_NEW").getName());
    }

    // ---- UserRolesServiceImpl -----------------------------------------------------------------------

    @Test
    void roleNamesAreLookedUpInOneQuery() {
        when(userRolesRepo.findRoleIdsByUserId(7L)).thenReturn(List.of(1L, 2L));
        when(roleRepo.findNamesByIdIn(List.of(1L, 2L))).thenReturn(List.of("ROLE_ADMIN", "ROLE_USER"));

        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), userRoles.getRoleNamesForUser(7L));
        assertEquals(List.of(1L, 2L), userRoles.getRoleIdsForUser(7L));
    }

    @Test
    void aUserWithoutRolesSkipsTheNameQuery() {
        when(userRolesRepo.findRoleIdsByUserId(8L)).thenReturn(List.of());
        assertEquals(List.of(), userRoles.getRoleNamesForUser(8L));
        verify(roleRepo, never()).findNamesByIdIn(any());
    }

    @Test
    void addRoleToUserIsIdempotent() {
        when(userRolesRepo.existsByUserIdAndRoleId(7L, 1L)).thenReturn(true);
        when(userRolesRepo.existsByUserIdAndRoleId(7L, 2L)).thenReturn(false);

        userRoles.addRoleToUser(7L, 1L);
        userRoles.addRoleToUser(7L, 2L);

        verify(userRolesRepo, times(1)).save(any(UserRoles.class));
        verify(userRolesRepo).save(new UserRoles(7L, 2L));
    }

    @Test
    void removeReplaceAndCheck() {
        when(userRolesRepo.deleteByUserIdAndRoleId(7L, 1L)).thenReturn(1);
        when(userRolesRepo.existsByUserIdAndRoleId(7L, 1L)).thenReturn(true);

        assertEquals(1, userRoles.removeRoleFromUser(7L, 1L));
        userRoles.replaceRolesForUser(7L, List.of(2L));
        verify(userRolesRepo).replaceRolesForUser(7L, List.of(2L));
        assertTrue(userRoles.userHasRole(7L, 1L));
    }
}
