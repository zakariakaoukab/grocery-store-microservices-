package com.epicerie.auth.controller;

import com.epicerie.auth.dto.AuthResponse;
import com.epicerie.auth.dto.LoginRequest;
import com.epicerie.auth.dto.RegisterRequest;
import com.epicerie.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Missing Authorization header"));
        }
        try {
            return ResponseEntity.ok(authService.validateToken(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "error", e.getMessage()));
        }
    }
}
