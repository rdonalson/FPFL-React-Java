package com.financialplanner.moduleapi.support;

import com.financialplanner.moduleapi.exception.GlobalExceptionHandler;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleapi.security.CustomUserDetails;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import com.financialplanner.modulecommonbc.sanitizer.SanitizerImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Builds a standalone MockMvc around real controllers (with mocked services), the real response factory and
 * sanitizer, and the real GlobalExceptionHandler — so tests see the same status codes and JSON a client does.
 */
public final class ApiTestSupport {

    public static final MediaType JSON = MediaType.APPLICATION_JSON;

    private ApiTestSupport() {}

    public static ApiResponseFactory responses() {
        return new ApiResponseFactory(new SanitizerImpl());
    }

    public static MockMvc mvc(Object... controllers) {
        return MockMvcBuilders.standaloneSetup(controllers)
                              .setControllerAdvice(new GlobalExceptionHandler())
                              .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                              .build();
    }

    /** Runs a request and returns the response; never throws for 4xx/5xx. */
    public static Result call(MockMvc mvc, RequestBuilder request) {
        try {
            MvcResult r = mvc.perform(request).andReturn();
            return new Result(r.getResponse().getStatus(), r.getResponse().getContentAsString(StandardCharsets.UTF_8),
                              r.getResponse().getHeader("Location"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public record Result(int status, String body, String location) {
        public boolean has(String text) {
            return body.contains(text);
        }
    }

    /** Puts a signed-in user in the security context, as JwtAuthFilter would. */
    public static CustomUserDetails signIn(UUID userId, String... roles) {
        return signIn(1L, userId, roles);
    }

    /** As {@link #signIn(UUID, String...)}, with the user's record id (the id change-password uses). */
    public static CustomUserDetails signIn(Long id, UUID userId, String... roles) {
        User user = new User();
        user.setId(id);
        user.setEmail("rick@example.com");
        user.setUserId(userId);
        CustomUserDetails details = new CustomUserDetails(user, List.of(roles));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
        return details;
    }

    public static void signOut() {
        SecurityContextHolder.clearContext();
    }
}
