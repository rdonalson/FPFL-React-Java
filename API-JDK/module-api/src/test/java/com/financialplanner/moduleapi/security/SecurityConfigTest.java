package com.financialplanner.moduleapi.security;

import com.financialplanner.moduleauth.domain.service.UserRolesService;
import com.financialplanner.moduleauth.domain.service.UserService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** The real SecurityConfig filter chain in front of stub endpoints: who gets in where. */
class SecurityConfigTest {

    private static AnnotationConfigWebApplicationContext ctx;
    private static MockMvc mvc;
    private static final JwtServiceImpl JWT = JwtServiceImplTest.service(60_000);

    @RestController
    static class Stub {
        @RequestMapping({"/auth/login", "/actuator/health", "/item-types", "/time-periods", "/items/x", "/client-logs",
                         "/debts", "/display/ledger", "/users/7/roles", "/initial-amount/x", "/v3/api-docs",
                         "/auth/change-password", "/items"})
        public String ok() {
            return "ok";
        }
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, Stub.class})
    static class TestConfig {
        @Bean
        UserService userService() {
            UserService users = mock(UserService.class);
            User rick = new User();
            rick.setId(7L);
            rick.setEmail("rick@example.com");
            rick.setUserId(UUID.randomUUID());
            when(users.findByEmail("rick@example.com")).thenReturn(Optional.of(rick));
            User admin = new User();
            admin.setId(8L);
            admin.setEmail("admin@example.com");
            admin.setUserId(UUID.randomUUID());
            when(users.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
            return users;
        }

        @Bean
        UserRolesService userRolesService() {
            UserRolesService roles = mock(UserRolesService.class);
            when(roles.getRoleNamesForUser(anyLong())).thenReturn(List.of("ROLE_USER"));
            when(roles.getRoleNamesForUser(8L)).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));
            return roles;
        }

        @Bean
        JwtService jwtService() {
            return JWT;
        }

        @Bean
        JwtAuthFilter jwtAuthFilter(JwtService jwt, UserService users, UserRolesService roles) {
            return new JwtAuthFilter(jwt, users, roles);
        }
    }

    @BeforeAll
    static void start() {
        ctx = new AnnotationConfigWebApplicationContext();
        ctx.setServletContext(new MockServletContext());
        ctx.register(TestConfig.class);
        ctx.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(ctx)
                             .addFilters(ctx.getBean("springSecurityFilterChain", Filter.class))
                             .build();
    }

    @AfterAll
    static void stop() {
        ctx.close();
    }

    private static int status(org.springframework.test.web.servlet.RequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse().getStatus();
    }

    private static String bearer() {
        return "Bearer " + JWT.generateToken("rick@example.com", Map.of("roles", List.of("ROLE_USER")));
    }

    private static String adminBearer() {
        return "Bearer " + JWT.generateToken("admin@example.com", Map.of("roles", List.of("ROLE_USER", "ROLE_ADMIN")));
    }

    @Test
    void publicEndpointsNeedNoToken() throws Exception {
        assertEquals(200, status(post("/auth/login")));
        assertEquals(200, status(get("/actuator/health")));
        assertEquals(200, status(get("/item-types")));
        assertEquals(200, status(get("/time-periods")));
        assertEquals(200, status(post("/client-logs")));
        assertEquals(200, status(get("/v3/api-docs")));
    }

    @Test
    void everythingElseNeedsASignedInUser() throws Exception {
        for (String path : List.of("/debts", "/display/ledger", "/users/7/roles", "/initial-amount/x", "/items",
                                   "/items/x")) {
            assertEquals(403, status(get(path)), path);
        }
        // Changing a password is under /auth but is not public
        assertEquals(403, status(post("/auth/change-password")));
        assertEquals(200, status(post("/auth/change-password").header("Authorization", bearer())));
    }

    @Test
    void itemsNeedASignedInUser() throws Exception {
        assertEquals(200, status(get("/items/x").header("Authorization", bearer())));
        assertEquals(200, status(delete("/items/x").header("Authorization", bearer())));
    }

    @Test
    void roleManagementIsForAdminsOnly() throws Exception {
        // An ordinary user cannot read or grant roles (no granting yourself ROLE_ADMIN)
        assertEquals(403, status(get("/users/7/roles").header("Authorization", bearer())));
        assertEquals(403, status(post("/users/7/roles").header("Authorization", bearer())));
        assertEquals(200, status(get("/users/7/roles").header("Authorization", adminBearer())));
        assertEquals(200, status(post("/users/7/roles").header("Authorization", adminBearer())));
    }

    @Test
    void catalogsAreReadableByAnyoneButOnlyAdminsChangeThem() throws Exception {
        for (String path : List.of("/item-types", "/time-periods")) {
            assertEquals(200, status(get(path)), path);
            assertEquals(403, status(post(path)), path);
            assertEquals(403, status(put(path).header("Authorization", bearer())), path);
            assertEquals(403, status(delete(path).header("Authorization", bearer())), path);
            assertEquals(200, status(post(path).header("Authorization", adminBearer())), path);
        }
    }

    @Test
    void preflightIsAlwaysAllowed() throws Exception {
        assertNotEquals(403, status(options("/users/7/roles").header("Origin", "http://localhost:4000")
                                                            .header("Access-Control-Request-Method", "POST")));
    }

    @Test
    void aValidTokenGetsIn() throws Exception {
        for (String path : List.of("/debts", "/display/ledger", "/initial-amount/x")) {
            assertEquals(200, status(get(path).header("Authorization", bearer())), path);
        }
    }

    @Test
    void aBadTokenDoesNot() throws Exception {
        String forged = "Bearer " + io.jsonwebtoken.Jwts.builder().setSubject("rick@example.com")
                                                                 .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256,
                                                                           "c29tZS1vdGhlci1zZWNyZXQtc29tZS1vdGhlci1zZWNyZXQtMTIzNDU2Nzg5MA==")
                                                                 .compact();
        // Rejected cleanly instead of an exception (a 500) escaping the filter
        assertEquals(403, status(get("/debts").header("Authorization", forged)));
        assertEquals(403, status(get("/debts").header("Authorization", "Bearer not.a.jwt")));
    }

    @Test
    void corsAllowsTheUiOriginsOnly() {
        CorsConfigurationSource source = ctx.getBean("corsConfigurationSource", CorsConfigurationSource.class);
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/items"));
        assertNotNull(cors);
        assertEquals(List.of("https://ui-tsx.ledger-finance.com", "http://localhost:4000", "http://localhost:9000"),
                     cors.getAllowedOrigins());
        assertTrue(cors.getAllowCredentials());
        assertNull(cors.checkOrigin("https://evil.example.com"));
        assertEquals("http://localhost:4000", cors.checkOrigin("http://localhost:4000"));
    }
}
