package com.financialplanner.moduleapi.security;

import com.financialplanner.moduleauth.domain.service.UserRolesService;
import com.financialplanner.moduleauth.domain.service.UserService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserRolesService userRolesService;

    public JwtAuthFilter(JwtService jwtService,
                         UserService userService,
                         UserRolesService userRolesService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRolesService = userRolesService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String email;
        try {
            email = jwtService.extractUsername(jwt);
        } catch (JwtException | IllegalArgumentException ex) {
            // Expired, forged or malformed token: carry on unauthenticated so the request gets a clean
            // 401/403 from Spring Security instead of an exception (500) out of the filter.
            filterChain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userService.findByEmail(email).orElse(null);

            if (user != null && jwtService.isTokenValid(jwt, user.getEmail())) {

                // Fetch DB-backed roles
                List<String> roleNames = userRolesService.getRoleNamesForUser(user.getId());

                // Updated constructor
                CustomUserDetails userDetails = new CustomUserDetails(user, roleNames);

                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
