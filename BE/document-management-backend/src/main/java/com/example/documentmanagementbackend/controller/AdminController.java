package com.example.documentmanagementbackend.controller;

import com.example.documentmanagementbackend.dto.request.CreateUserRequest;
import com.example.documentmanagementbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    /**
     * API tạo người dùng mới (chỉ dành cho Admin)
     * @param request Thông tin người dùng cần tạo
     * @param bindingResult Kết quả validation
     * @return ResponseEntity chứa thông tin người dùng đã tạo hoặc thông báo lỗi
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody CreateUserRequest request,
            BindingResult bindingResult
    ) {
        // Bắt lỗi Validation ngay lập tức nếu có
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        return userService.createUser(request);
    }
}
