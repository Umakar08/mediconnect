package com.mediconnect.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
public class SecureController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "message", "API key accepted");
    }
}
