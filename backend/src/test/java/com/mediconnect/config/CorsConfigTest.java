package com.mediconnect.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

class CorsConfigTest {
    @Test
    void allowsConfiguredOriginsAndApiHeaders() {
        CorsConfig config = new CorsConfig();
        var source = config.corsConfigurationSource(
                "https://mediconnect.example, https://preview.example");

        CorsConfiguration cors = source.getCorsConfiguration(
                new MockHttpServletRequest("OPTIONS", "/api/health"));

        assertEquals(2, cors.getAllowedOrigins().size());
        assertTrue(cors.getAllowedOrigins().contains("https://mediconnect.example"));
        assertTrue(cors.getAllowedOrigins().contains("https://preview.example"));
        assertTrue(cors.getAllowedMethods().contains("POST"));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
        assertNull(cors.getAllowCredentials());
    }
}
