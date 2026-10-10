package com.mediconnect.service;

import com.mediconnect.dto.AuthRequests;
import com.mediconnect.dto.AuthResponse;
import com.mediconnect.entity.AccessToken;
import com.mediconnect.entity.User;
import com.mediconnect.repository.AccessTokenRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.TokenHash;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, AccessTokenRepository accessTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(AuthRequests.Register request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        User user = userRepository.save(new User(request.name().trim(), email, passwordEncoder.encode(request.password())));
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), null, "Account created");
    }

    public AuthResponse login(AuthRequests.Login request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        accessTokenRepository.deleteByExpiresAtBefore(Instant.now());
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        accessTokenRepository.save(new AccessToken(TokenHash.sha256(rawToken), user, Instant.now().plus(12, ChronoUnit.HOURS)));
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), rawToken, "Signed in successfully");
    }

    public void logout(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            accessTokenRepository.deleteByTokenHash(TokenHash.sha256(rawToken));
        }
    }

    public AuthResponse currentUser(User user) {
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), null, "Signed in");
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
