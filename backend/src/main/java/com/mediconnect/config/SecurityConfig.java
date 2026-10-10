package com.mediconnect.config;

import com.mediconnect.security.ApiKeyAuthenticationFilter;
import com.mediconnect.security.BearerTokenAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter;

    public SecurityConfig(ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter) {
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
        this.bearerTokenAuthenticationFilter = bearerTokenAuthenticationFilter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/health", "/error").permitAll()
                        .requestMatchers("/api/auth/me", "/api/auth/logout")
                        .hasAnyRole("PATIENT", "STAFF", "ADMIN")
                        .requestMatchers("/api/patient/**").hasRole("PATIENT")
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/secure/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(bearerTokenAuthenticationFilter, ApiKeyAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                "A valid bearer token is required")))
                .build();
    }
}
