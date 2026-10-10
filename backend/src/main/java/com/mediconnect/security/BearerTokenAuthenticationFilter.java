package com.mediconnect.security;

import com.mediconnect.repository.AccessTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    private final AccessTokenRepository accessTokenRepository;

    public BearerTokenAuthenticationFilter(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String rawToken = authorization.substring(7).trim();
            if (!rawToken.isEmpty()) {
                accessTokenRepository.findByTokenHashAndExpiresAtAfter(TokenHash.sha256(rawToken), java.time.Instant.now())
                        .ifPresent(token -> {
                            var user = token.getUser();
                            var authentication = new UsernamePasswordAuthenticationToken(
                                    user, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            }
        }
        filterChain.doFilter(request, response);
    }
}
