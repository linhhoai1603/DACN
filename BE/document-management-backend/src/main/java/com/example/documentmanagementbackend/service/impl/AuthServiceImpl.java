package com.example.documentmanagementbackend.service.impl;

import com.example.documentmanagementbackend.dto.response.RegisterResponse;
import com.example.documentmanagementbackend.entity.Role;
import com.example.documentmanagementbackend.entity.User;
import com.example.documentmanagementbackend.dto.response.LoginResponse;
import com.example.documentmanagementbackend.exception.BadRequestException;
import com.example.documentmanagementbackend.repository.RoleRepository;
import com.example.documentmanagementbackend.repository.UserRepository;
import com.example.documentmanagementbackend.security.JwtTokenProvider;
import com.example.documentmanagementbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails principal = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(principal);
            return new LoginResponse(token, "Bearer", jwtTokenProvider.getJwtExpirationMs());
        } catch (AuthenticationException ex) {
            throw new BadRequestException("Invalid username or password");
        }
    }

    @Override
    public RegisterResponse register(String username, String rawPassword, String email, String fullName) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        Role defaultRole = roleRepository.findByName("ROLE_MODERATOR")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_MODERATOR");
                    role.setPermissions(Set.of());
                    return roleRepository.save(role);
                });

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setIsActive(true);
        user.setRoles(Set.of(defaultRole));

        User savedUser = userRepository.save(user);
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getIsActive()
        );
    }

    @Override
    public RegisterResponse createUserByAdmin(
            String username,
            String rawPassword,
            String email,
            String fullName,
            String roleName,
            Boolean isActive
    ) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        String targetRole = (roleName == null || roleName.isBlank()) ? "ROLE_MODERATOR" : roleName;
        Role role = roleRepository.findByName(targetRole)
                .orElseThrow(() -> new BadRequestException("Role not found: " + targetRole));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setIsActive(isActive == null ? Boolean.TRUE : isActive);
        user.setRoles(Set.of(role));

        User savedUser = userRepository.save(user);
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getIsActive()
        );
    }
}
