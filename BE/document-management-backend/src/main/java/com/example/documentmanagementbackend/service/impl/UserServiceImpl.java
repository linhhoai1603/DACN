package com.example.documentmanagementbackend.service.impl;

import com.example.documentmanagementbackend.dto.request.CreateUserRequest;
import com.example.documentmanagementbackend.dto.response.CreateUserResponse;
import com.example.documentmanagementbackend.model.User;
import com.example.documentmanagementbackend.repository.UserRepository;
import com.example.documentmanagementbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<?> createUser(CreateUserRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Kiểm tra email đã tồn tại chưa
        if (existsByEmail(request.getEmail())) {
            errors.put("email", "Email đã tồn tại trong hệ thống");
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        if (existsByPhone(request.getPhone())) {
            errors.put("phone", "Số điện thoại đã tồn tại trong hệ thống");
        }

        // Nếu có lỗi, trả về danh sách lỗi
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
        }

        // Tạo user mới
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAddress(request.getAddress());
        user.setRole(request.getRole());

        // Lưu user vào database
        User savedUser = userRepository.save(user);

        // Tạo response
        CreateUserResponse response = CreateUserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .address(savedUser.getAddress())
                .role(savedUser.getRole())
                .message("Tạo người dùng thành công")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }
}
