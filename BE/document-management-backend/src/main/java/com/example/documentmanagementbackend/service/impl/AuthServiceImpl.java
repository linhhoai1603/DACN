package com.example.documentmanagementbackend.service.impl;

import com.example.documentmanagementbackend.dto.request.LoginRequest;
import com.example.documentmanagementbackend.model.User;
import com.example.documentmanagementbackend.repository.UserRepository;
import com.example.documentmanagementbackend.service.AuthService;
import com.example.documentmanagementbackend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public ResponseEntity<String> login(LoginRequest request ) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Email not found");
        }

        if (!user.getPhone().equals(request.getPhoneNumber())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User with matching phone not found");
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(token);
    }
}
