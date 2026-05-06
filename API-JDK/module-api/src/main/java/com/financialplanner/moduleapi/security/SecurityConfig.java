package com.financialplanner.moduleapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Frontend + static assets
                .requestMatchers(
                    "/",
                    "/home",
                    "/index.html",
                    "/favicon.ico",
                    "/static/**",
                    "/assets/**",
                    "/images/**",
                    "/icon.svg"
                                ).permitAll()

                // Actuator health
                .requestMatchers("/actuator/health").permitAll()

                // Allow authentication endpoints
                .requestMatchers("/auth/**")
                .permitAll()

                // Allow item-related endpoints
                .requestMatchers("/item-types/**", "/time-periods/**", "/items/**")
                .permitAll()
                // Allow Swagger UI + OpenAPI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .permitAll()

                // Everything else requires JWT
                .anyRequest()
                .authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
