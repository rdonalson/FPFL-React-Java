package com.financialplanner.moduleauth.infrastructure.persistence.adapter;

import com.financialplanner.moduleauth.infrastructure.persistence.adapter.custom.JpaUserRolesCustomRepositoryImpl;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.*;
import com.financialplanner.moduleauth.infrastructure.persistence.repository.*;
import com.financialplanner.moduleauth.infrastructure.persistence.repository.custom.JpaUserRolesCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** The adapters hand every call to Spring Data unchanged; the custom role replacement batches its inserts. */
class AuthRepositoryAdaptersTest {

    @Test
    void userRepositoryDelegates() {
        JpaUserRepository jpa = mock(JpaUserRepository.class);
        UserRepositoryImpl repo = new UserRepositoryImpl(jpa);
        User u = new User();
        UUID id = UUID.randomUUID();
        when(jpa.findByEmail("e")).thenReturn(Optional.of(u));
        when(jpa.findById(1L)).thenReturn(Optional.of(u));
        when(jpa.findByUserId(id)).thenReturn(Optional.of(u));
        when(jpa.save(u)).thenReturn(u);

        assertSame(u, repo.findByEmail("e").orElseThrow());
        assertSame(u, repo.findById(1L).orElseThrow());
        assertSame(u, repo.findByUserId(id).orElseThrow());
        assertSame(u, repo.save(u));
    }

    @Test
    void roleRepositoryDelegates() {
        JpaRoleRepository jpa = mock(JpaRoleRepository.class);
        RoleRepositoryImpl repo = new RoleRepositoryImpl(jpa);
        Role r = new Role("ROLE_USER");
        when(jpa.findByName("ROLE_USER")).thenReturn(Optional.of(r));
        when(jpa.findById(2L)).thenReturn(Optional.of(r));
        when(jpa.findNamesByIdIn(List.of(2L))).thenReturn(List.of("ROLE_USER"));
        when(jpa.save(r)).thenReturn(r);

        assertSame(r, repo.findByName("ROLE_USER").orElseThrow());
        assertSame(r, repo.findById(2L).orElseThrow());
        assertEquals(List.of("ROLE_USER"), repo.findNamesByIdIn(List.of(2L)));
        assertSame(r, repo.save(r));
    }

    @Test
    void refreshTokenRepositoryDelegates() {
        JpaRefreshTokenRepository jpa = mock(JpaRefreshTokenRepository.class);
        RefreshTokenRepositoryImpl repo = new RefreshTokenRepositoryImpl(jpa);
        RefreshToken t = new RefreshToken();
        User u = new User();
        when(jpa.findByToken("x")).thenReturn(Optional.of(t));
        when(jpa.save(t)).thenReturn(t);

        assertSame(t, repo.findByToken("x").orElseThrow());
        assertSame(t, repo.save(t));
        repo.delete(t);
        repo.deleteByUser(u);
        verify(jpa).delete(t);
        verify(jpa).deleteByUser(u);
    }

    @Test
    void userRolesRepositoryDelegates() {
        JpaUserRolesRepository jpa = mock(JpaUserRolesRepository.class);
        JpaUserRolesCustomRepository custom = mock(JpaUserRolesCustomRepository.class);
        UserRolesRepositoryImpl repo = new UserRolesRepositoryImpl(jpa, custom);
        UserRoles ur = new UserRoles(7L, 1L);
        when(jpa.findRoleIdsByUserId(7L)).thenReturn(List.of(1L));
        when(jpa.findUserIdsByRoleId(1L)).thenReturn(List.of(7L));
        when(jpa.findByUserId(7L)).thenReturn(List.of(ur));
        when(jpa.findByUserIdAndRoleId(7L, 1L)).thenReturn(Optional.of(ur));
        when(jpa.existsByUserIdAndRoleId(7L, 1L)).thenReturn(true);
        when(jpa.save(ur)).thenReturn(ur);
        when(jpa.deleteByUserIdAndRoleId(7L, 1L)).thenReturn(1);
        when(jpa.deleteByUserId(7L)).thenReturn(2);

        assertEquals(List.of(1L), repo.findRoleIdsByUserId(7L));
        assertEquals(List.of(7L), repo.findUserIdsByRoleId(1L));
        assertEquals(List.of(ur), repo.findByUserId(7L));
        assertSame(ur, repo.findByUserIdAndRoleId(7L, 1L).orElseThrow());
        assertTrue(repo.existsByUserIdAndRoleId(7L, 1L));
        assertSame(ur, repo.save(ur));
        assertEquals(1, repo.deleteByUserIdAndRoleId(7L, 1L));
        assertEquals(2, repo.deleteByUserId(7L));
        repo.replaceRolesForUser(7L, List.of(1L, 2L));
        verify(custom).replaceRolesForUser(7L, List.of(1L, 2L));
    }

    @Test
    void replaceRolesDeletesThenInsertsEachRole() throws Exception {
        EntityManager em = mock(EntityManager.class);
        Query delete = mock(Query.class);
        when(em.createQuery(startsWith("DELETE FROM UserRoles"))).thenReturn(delete);
        when(delete.setParameter("userId", 7L)).thenReturn(delete);
        JpaUserRolesCustomRepositoryImpl repo = withEntityManager(em);

        repo.replaceRolesForUser(7L, List.of(1L, 2L));

        verify(delete).executeUpdate();
        verify(em).persist(new UserRoles(7L, 1L));
        verify(em).persist(new UserRoles(7L, 2L));
        verify(em).flush();
    }

    @Test
    void replaceRolesWithAnEmptyListOnlyClears() throws Exception {
        EntityManager em = mock(EntityManager.class);
        Query delete = mock(Query.class);
        when(em.createQuery(anyString())).thenReturn(delete);
        when(delete.setParameter(anyString(), any())).thenReturn(delete);
        JpaUserRolesCustomRepositoryImpl repo = withEntityManager(em);

        repo.replaceRolesForUser(7L, List.of());

        verify(delete).executeUpdate();
        verify(em, never()).persist(any());
        assertThrows(IllegalArgumentException.class, () -> repo.replaceRolesForUser(7L, null));
    }

    private static JpaUserRolesCustomRepositoryImpl withEntityManager(EntityManager em) throws Exception {
        JpaUserRolesCustomRepositoryImpl repo = new JpaUserRolesCustomRepositoryImpl();
        Field f = JpaUserRolesCustomRepositoryImpl.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(repo, em);
        return repo;
    }
}
