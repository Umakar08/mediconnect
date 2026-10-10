package com.mediconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private final String configuredApiKey;

    public ApiKeyAuthenticationFilter(@Value("${app.security.api-key}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String suppliedApiKey = request.getHeader(API_KEY_HEADER);
        if (SecurityContextHolder.getContext().getAuthentication() == null && isValid(suppliedApiKey)) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "api-key-client", null, AuthorityUtils.NO_AUTHORITIES);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isValid(String suppliedApiKey) {
        return configuredApiKey != null && !configuredApiKey.isBlank()
                && suppliedApiKey != null
                && MessageDigest.isEqual(
                        suppliedApiKey.getBytes(StandardCharsets.UTF_8),
                        configuredApiKey.getBytes(StandardCharsets.UTF_8));
    }
}
