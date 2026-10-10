package com.mediconnect.controller;

import com.mediconnect.dto.AuthResponse;
import com.mediconnect.dto.CareRequests;
import com.mediconnect.service.StaffAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174",
        "https://frontend-three-rho-24.vercel.app" })
public class AdminController {
    private final StaffAccountService staffAccountService;

    public AdminController(StaffAccountService staffAccountService) {
        this.staffAccountService = staffAccountService;
    }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse createStaff(@Valid @RequestBody CareRequests.StaffAccount account) {
        return staffAccountService.createStaff(account);
    }
}
