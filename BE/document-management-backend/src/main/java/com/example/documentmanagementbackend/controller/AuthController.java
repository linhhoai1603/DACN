package com.example.documentmanagementbackend.controller;

import com.example.documentmanagementbackend.dto.ApiResponse;
import com.example.documentmanagementbackend.dto.request.LoginRequest;
import com.example.documentmanagementbackend.dto.request.RegisterRequest;
import com.example.documentmanagementbackend.dto.response.LoginResponse;
import com.example.documentmanagementbackend.dto.response.RegisterResponse;
import com.example.documentmanagementbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getFullName()
        );
        return ResponseEntity.ok(ApiResponse.success("Register successful", response));
    }
}
