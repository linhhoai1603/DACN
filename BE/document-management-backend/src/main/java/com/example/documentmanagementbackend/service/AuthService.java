package com.example.documentmanagementbackend.service;

import com.example.documentmanagementbackend.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<String> login(LoginRequest request);
}
