package com.mediconnect.config;

import com.mediconnect.entity.User;
import com.mediconnect.entity.UserRole;
import com.mediconnect.repository.UserRepository;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(UserRepository userRepository, JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.email:}") String adminEmail,
            @Value("${app.bootstrap-admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbcTemplate.update("UPDATE users SET role = 'PATIENT' WHERE role IS NULL OR role = ''");
        if (adminEmail.isBlank() && adminPassword.isBlank()) return;
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Configure both MEDICONNECT_ADMIN_EMAIL and MEDICONNECT_ADMIN_PASSWORD to bootstrap the administrator");
        }

        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        User admin = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(() ->
                userRepository.save(new User("MediConnect Administrator", normalizedEmail,
                        passwordEncoder.encode(adminPassword), UserRole.ADMIN)));
        if (admin.getRole() != UserRole.ADMIN) {
            admin.setRole(UserRole.ADMIN);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        }
    }
}
