package com.financialplanner.moduleapi.security;

import com.financialplanner.moduleauth.domain.service.UserRolesService;
import com.financialplanner.moduleauth.domain.service.UserService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private final JwtService jwt = mock(JwtService.class);
    private final UserService users = mock(UserService.class);
    private final UserRolesService roles = mock(UserRolesService.class);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwt, users, roles);
    private final FilterChain chain = mock(FilterChain.class);
    private final User rick = new User();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        rick.setId(7L);
        rick.setEmail("rick@example.com");
        rick.setUserId(UUID.randomUUID());
        rick.setPasswordHash("hash");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest request(String authorization) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/debts");
        if (authorization != null) req.addHeader("Authorization", authorization);
        return req;
    }

    @Test
    void aValidBearerTokenSignsTheUserInWithTheirRoles() throws Exception {
        when(jwt.extractUsername("good")).thenReturn("rick@example.com");
        when(users.findByEmail("rick@example.com")).thenReturn(Optional.of(rick));
        when(jwt.isTokenValid("good", "rick@example.com")).thenReturn(true);
        when(roles.getRoleNamesForUser(7L)).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

        MockHttpServletRequest req = request("Bearer good");
        filter.doFilter(req, new MockHttpServletResponse(), chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        assertSame(rick, principal.getUser());
        assertEquals("rick@example.com", principal.getUsername());
        assertEquals("hash", principal.getPassword());
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"),
                     auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList());
        verify(chain).doFilter(eq(req), any());
    }

    @Test
    void noHeaderOrANonBearerHeaderPassesThroughUnauthenticated() throws Exception {
        filter.doFilter(request(null), new MockHttpServletResponse(), chain);
        filter.doFilter(request("Basic abc"), new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(2)).doFilter(any(), any());
        verifyNoInteractions(jwt);
    }

    @Test
    void aTokenThatCannotBeParsedIsIgnoredNotThrown() throws Exception {
        when(jwt.extractUsername("expired")).thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expired"));
        when(jwt.extractUsername("junk")).thenThrow(new io.jsonwebtoken.MalformedJwtException("junk"));

        filter.doFilter(request("Bearer expired"), new MockHttpServletResponse(), chain);
        filter.doFilter(request("Bearer junk"), new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(2)).doFilter(any(), any());
    }

    @Test
    void anInvalidTokenOrUnknownUserIsNotSignedIn() throws Exception {
        when(jwt.extractUsername("stale")).thenReturn("rick@example.com");
        when(users.findByEmail("rick@example.com")).thenReturn(Optional.of(rick));
        when(jwt.isTokenValid("stale", "rick@example.com")).thenReturn(false);
        when(jwt.extractUsername("ghost")).thenReturn("ghost@example.com");
        when(users.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        filter.doFilter(request("Bearer stale"), new MockHttpServletResponse(), chain);
        filter.doFilter(request("Bearer ghost"), new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(2)).doFilter(any(), any());
    }
}
