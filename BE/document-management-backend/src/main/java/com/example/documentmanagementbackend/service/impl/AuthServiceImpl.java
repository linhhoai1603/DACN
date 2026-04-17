package com.example.documentmanagementbackend.service.impl;

import com.example.documentmanagementbackend.dto.request.LoginRequest;
import com.example.documentmanagementbackend.dto.request.RegisterRequest;
import com.example.documentmanagementbackend.model.Role;
import com.example.documentmanagementbackend.model.User;
import com.example.documentmanagementbackend.repository.UserRepository;
import com.example.documentmanagementbackend.service.AuthService;
import com.example.documentmanagementbackend.service.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> login(LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }
        var userLogin = user.get();
        if (!userLogin.getPhone().equals(request.getPhoneNumber())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Phone number does not match");
        }
        if (!passwordEncoder.matches(request.getPassword(), userLogin.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password is incorrect");
        }
        String token = jwtService.generateToken(userLogin);
        return ResponseEntity.ok(token);
    }

    @Override
    public ResponseEntity<String> register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email đã được sử dụng");
        }
        if (userRepository.findByPhone(request.getPhoneNumber()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Số điện thoại đã được sử dụng");
        }

        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhoneNumber());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setAddress(request.getAddress());
        try {
            newUser.setRole(Role.fromSignupValue(request.getRole()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }

        userRepository.save(newUser);
        String token = jwtService.generateToken(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }
}
