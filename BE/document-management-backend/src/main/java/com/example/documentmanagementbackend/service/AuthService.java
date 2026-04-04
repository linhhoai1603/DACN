package com.example.documentmanagementbackend.service;

import com.example.documentmanagementbackend.dto.response.LoginResponse;
import com.example.documentmanagementbackend.dto.response.RegisterResponse;

public interface AuthService {
    LoginResponse login(String username, String password);

    RegisterResponse register(String username, String rawPassword, String email, String fullName);
}
