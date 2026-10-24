package com.financialplanner.moduleapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Enable CORS using our bean below
            .cors(Customizer.withDefaults())

            // Disable CSRF for stateless JWT APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                // Allow preflight OPTIONS requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Static + frontend assets
                .requestMatchers("/", "/home", "/index.html", "/favicon.ico", "/static/**", "/assets/**", "/images/**", "/icon.svg").permitAll()

                // Actuator health
                .requestMatchers("/actuator/health").permitAll()

                // Authentication endpoints (changing a password needs the signed-in user)
                .requestMatchers("/auth/change-password").authenticated()
                .requestMatchers("/auth/**").permitAll()

                // Client log endpoint
                .requestMatchers("/client-logs").permitAll()

                // Catalogs: anyone may read them, only admins may change them
                .requestMatchers(HttpMethod.GET, "/item-types/**", "/time-periods/**").permitAll()
                .requestMatchers("/item-types/**", "/time-periods/**").hasAuthority("ROLE_ADMIN")

                // Role management is admin-only
                .requestMatchers("/users/**").hasAuthority("ROLE_ADMIN")

                // Swagger / OpenAPI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Everything else (items, initial amount, display, debts) requires a JWT;
                // the controllers then limit each user to their own data
                .anyRequest().authenticated())

            // Stateless JWT sessions
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Add your JWT filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // AuthenticationManager for login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ============================
    // CORS CONFIGURATION
    // ============================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Your frontend origin
        config.setAllowedOrigins(List.of(
            "https://ui-tsx.ledger-finance.com", // production
            "http://localhost:4000",             // Vite dev server
            "http://localhost:9000"              // UI-TSX in Docker (9000:80)
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allowed request headers
        config.setAllowedHeaders(List.of("*"));

        // Exposed response headers
        config.setExposedHeaders(List.of("*"));

        // Allow Authorization header
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
