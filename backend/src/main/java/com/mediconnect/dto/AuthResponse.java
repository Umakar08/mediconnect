package com.mediconnect.dto;

import com.mediconnect.entity.UserRole;

public record AuthResponse(Long id, String name, String email, UserRole role, String accessToken, String message) { }
