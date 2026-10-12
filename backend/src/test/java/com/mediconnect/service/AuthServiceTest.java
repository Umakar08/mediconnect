package com.mediconnect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mediconnect.dto.AuthRequests;
import com.mediconnect.entity.User;
import com.mediconnect.entity.UserRole;
import com.mediconnect.repository.AccessTokenRepository;
import com.mediconnect.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest {
    private UserRepository userRepository;
    private AccessTokenRepository accessTokenRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        accessTokenRepository = mock(AccessTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new AuthService(userRepository, accessTokenRepository, passwordEncoder);
    }

    @Test
    void staffCanSignInThroughStaffLogin() {
        User staff = staffUser();
        when(userRepository.findByEmailIgnoreCase("staff@example.test")).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);

        var response = service.staffLogin(new AuthRequests.Login("staff@example.test", "correct-password"));

        assertEquals(UserRole.STAFF, response.role());
        verify(accessTokenRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void staffCannotSignInThroughPatientLogin() {
        User staff = staffUser();
        when(userRepository.findByEmailIgnoreCase("staff@example.test")).thenReturn(Optional.of(staff));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> service.login(new AuthRequests.Login("staff@example.test", "correct-password")));

        verify(accessTokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void administratorCanSignInThroughStaffLogin() {
        User admin = staffUser();
        when(admin.getRole()).thenReturn(UserRole.ADMIN);
        when(userRepository.findByEmailIgnoreCase("staff@example.test")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);

        var response = service.staffLogin(new AuthRequests.Login("staff@example.test", "correct-password"));

        assertEquals(UserRole.ADMIN, response.role());
        verify(accessTokenRepository).save(org.mockito.ArgumentMatchers.any());
    }

    private User staffUser() {
        User staff = mock(User.class);
        when(staff.getId()).thenReturn(3L);
        when(staff.getName()).thenReturn("Sam Staff");
        when(staff.getEmail()).thenReturn("staff@example.test");
        when(staff.getPasswordHash()).thenReturn("hashed-password");
        when(staff.getRole()).thenReturn(UserRole.STAFF);
        return staff;
    }
}
