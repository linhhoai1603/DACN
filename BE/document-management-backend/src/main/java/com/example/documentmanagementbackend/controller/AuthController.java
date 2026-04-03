package com.example.documentmanagementbackend.controller;


import com.example.documentmanagementbackend.dto.request.LoginRequest;
import com.example.documentmanagementbackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult
    ) {
        // 1. Bắt lỗi Validation ngay lập tức nếu có
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            // Lặp qua các trường bị lỗi và lấy ra thông báo lỗi đã định nghĩa trong DTO
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }

            // Trả về HTTP 400 Bad Request kèm danh sách lỗi
            return ResponseEntity.badRequest().body(errors);
        }

        return authService.login(request);
    }
}
