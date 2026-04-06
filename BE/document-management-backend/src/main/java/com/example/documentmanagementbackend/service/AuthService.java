package com.example.documentmanagementbackend.service;

import com.example.documentmanagementbackend.dto.request.LoginRequest;
import com.example.documentmanagementbackend.dto.request.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<String> login(LoginRequest request);
    ResponseEntity<String> register(RegisterRequest request);
}
