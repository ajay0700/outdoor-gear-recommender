package com.outdoor.gear.controller;

import com.outdoor.gear.dto.AuthResponse;
import com.outdoor.gear.dto.LoginRequest;
import com.outdoor.gear.dto.RegisterRequest;
import com.outdoor.gear.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse resp = authService.login(request);
            return ResponseEntity.ok(resp);
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("message", "用户名或密码错误"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse resp = authService.register(request);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
}
