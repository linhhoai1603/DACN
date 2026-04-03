package com.example.documentmanagementbackend.service.impl;

import com.example.documentmanagementbackend.dto.request.LoginRequest;
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
        System.out.println(request.getEmail());
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Email not found");
        }
        var userLogin = user.get();
        if (!userLogin.getPhone().equals(request.getPhoneNumber())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User with matching phone not found");
        }

        if (!passwordEncoder.matches(request.getPassword(), userLogin.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Password is incorrect");
        }

        String token = jwtService.generateToken(userLogin);
        return ResponseEntity.ok(token);
    }
}
