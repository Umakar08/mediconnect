package com.mediconnect.service;

import com.mediconnect.dto.AuthResponse;
import com.mediconnect.dto.CareRequests;
import com.mediconnect.entity.User;
import com.mediconnect.entity.UserRole;
import com.mediconnect.repository.UserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StaffAccountService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffAccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse createStaff(CareRequests.StaffAccount account) {
        String email = account.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        User staff = userRepository.save(new User(account.name().trim(), email,
                passwordEncoder.encode(account.password()), UserRole.STAFF));
        return new AuthResponse(staff.getId(), staff.getName(), staff.getEmail(),
                staff.getRole(), null, "Staff account created");
    }
}
