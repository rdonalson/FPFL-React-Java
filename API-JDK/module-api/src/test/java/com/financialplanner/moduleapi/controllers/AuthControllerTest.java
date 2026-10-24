package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.security.JwtService;
import com.financialplanner.moduleapi.support.ApiTestSupport.Result;
import com.financialplanner.modulecommonbc.exception.InvalidCredentialsException;
import com.financialplanner.moduleauth.application.service.RefreshTokenServiceImpl;
import com.financialplanner.moduleauth.domain.service.AuthService;
import com.financialplanner.moduleauth.domain.service.UserRolesService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.RefreshToken;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.financialplanner.moduleapi.support.ApiTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class AuthControllerTest {

    private final AuthService auth = mock(AuthService.class);
    private final RefreshTokenServiceImpl refresh = mock(RefreshTokenServiceImpl.class);
    private final JwtService jwt = mock(JwtService.class);
    private final UserRolesService roles = mock(UserRolesService.class);
    private final MockMvc mvc = mvc(new AuthController(auth, refresh, jwt, responses(), roles));

    private final User rick = new User();

    @BeforeEach
    void setUp() {
        rick.setId(7L);
        rick.setEmail("rick@example.com");
        rick.setUserId(UUID.fromString("11111111-2222-3333-4444-555555555555"));
        rick.setFirst("Rick");
        rick.setLast("Donalson");
        RefreshToken token = new RefreshToken();
        token.setToken("refresh-1");
        token.setUser(rick);
        when(refresh.createRefreshToken(7L)).thenReturn(token);
        when(roles.getRoleNamesForUser(7L)).thenReturn(List.of("ROLE_USER"));
        when(jwt.generateToken(eq("rick@example.com"), anyMap())).thenReturn("jwt-access");
    }

    @AfterEach
    void tearDown() {
        signOut();
    }

    @Test
    void loginReturnsTokensAndTheUser() {
        when(auth.login("rick@example.com", "Password@2")).thenReturn(rick);

        Result r = call(mvc, post("/auth/login").contentType(JSON)
                                                .content("{\"email\":\"rick@example.com\",\"password\":\"Password@2\"}"));

        assertEquals(200, r.status(), r.body());
        assertTrue(r.has("\"accessToken\":\"jwt-access\""));
        assertTrue(r.has("\"refreshToken\":\"refresh-1\""));
        assertTrue(r.has("\"userId\":\"11111111-2222-3333-4444-555555555555\""));
        assertTrue(r.has("ROLE_USER"));
        verify(jwt).generateToken("rick@example.com", Map.of("roles", List.of("ROLE_USER")));
    }

    @Test
    void wrongCredentialsAre401() {
        when(auth.login(anyString(), anyString())).thenThrow(new InvalidCredentialsException("Invalid credentials"));
        Result r = call(mvc, post("/auth/login").contentType(JSON).content("{\"email\":\"rick@example.com\",\"password\":\"WrongPass1\"}"));
        assertEquals(401, r.status());
        assertTrue(r.has("Invalid credentials"));
    }

    @Test
    void anInvalidLoginBodyIs400NotAServerError() {
        Result r = call(mvc, post("/auth/login").contentType(JSON).content("{\"email\":\"not-an-email\",\"password\":\"short\"}"));
        assertEquals(400, r.status(), r.body());
        verifyNoInteractions(auth);
    }

    @Test
    void registerSignsTheNewUserIn() {
        when(auth.register("rick@example.com", "Password@2", "Rick", "Donalson")).thenReturn(rick);
        Result r = call(mvc, post("/auth/register").contentType(JSON).content(
            "{\"email\":\"rick@example.com\",\"password\":\"Password@2\",\"first\":\"Rick\",\"last\":\"Donalson\"}"));
        assertEquals(200, r.status(), r.body());
        assertTrue(r.has("Registration successful"));
        assertTrue(r.has("\"first\":\"Rick\""));
    }

    @Test
    void registerRequiresNames() {
        Result r = call(mvc, post("/auth/register").contentType(JSON).content(
            "{\"email\":\"rick@example.com\",\"password\":\"Password@2\",\"first\":\"\",\"last\":\"\"}"));
        assertEquals(400, r.status(), r.body());
        verifyNoInteractions(auth);
    }

    @Test
    void refreshIssuesNewTokensForAValidRefreshToken() {
        RefreshToken old = new RefreshToken();
        old.setToken("refresh-0");
        old.setUser(rick);
        when(refresh.findByToken("refresh-0")).thenReturn(old);

        Result r = call(mvc, post("/auth/refresh").contentType(JSON).content("{\"refreshToken\":\"refresh-0\"}"));

        assertEquals(200, r.status(), r.body());
        assertTrue(r.has("\"accessToken\":\"jwt-access\"") && r.has("\"refreshToken\":\"refresh-1\""));
        verify(refresh).verifyExpiration(old);
    }

    @Test
    void changePasswordIsOnlyForYourOwnAccount() {
        String body = "{\"id\":7,\"currentPassword\":\"Password@2\",\"newPassword\":\"NewPassword@3\"}";
        // Signed out
        assertEquals(401, call(mvc, post("/auth/change-password").contentType(JSON).content(body)).status());
        // Signed in as someone else
        signIn(8L, UUID.randomUUID(), "ROLE_USER");
        Result r = call(mvc, post("/auth/change-password").contentType(JSON).content(body));
        assertEquals(403, r.status());
        assertTrue(r.has("You can only change your own password"));
        verify(auth, never()).changePassword(anyLong(), anyString(), anyString());
        // An admin may
        when(auth.changePassword(7L, "Password@2", "NewPassword@3")).thenReturn(rick);
        signIn(8L, UUID.randomUUID(), "ROLE_USER", "ROLE_ADMIN");
        assertEquals(200, call(mvc, post("/auth/change-password").contentType(JSON).content(body)).status());
    }

    @Test
    void changePasswordReturnsFreshTokens() {
        signIn(7L, rick.getUserId(), "ROLE_USER");
        when(auth.changePassword(7L, "Password@2", "NewPassword@3")).thenReturn(rick);
        Result r = call(mvc, post("/auth/change-password").contentType(JSON)
                                                          .content("{\"id\":7,\"currentPassword\":\"Password@2\",\"newPassword\":\"NewPassword@3\"}"));
        assertEquals(200, r.status(), r.body());
        assertTrue(r.has("Password changed successfully"));
    }

    @Test
    void aWrongCurrentPasswordIs401() {
        signIn(7L, rick.getUserId(), "ROLE_USER");
        when(auth.changePassword(anyLong(), anyString(), anyString()))
            .thenThrow(new InvalidCredentialsException("Current password is incorrect"));
        Result r = call(mvc, post("/auth/change-password").contentType(JSON)
                                                          .content("{\"id\":7,\"currentPassword\":\"nope-nope\",\"newPassword\":\"NewPassword@3\"}"));
        assertEquals(401, r.status());
        assertTrue(r.has("Current password is incorrect"));
    }
}
