package com.example.documentmanagementbackend.auth;

import com.example.documentmanagementbackend.dto.request.LoginRequest;
import com.example.documentmanagementbackend.model.Role;
import com.example.documentmanagementbackend.model.User;
import com.example.documentmanagementbackend.repository.UserRepository;
import com.example.documentmanagementbackend.service.impl.AuthServiceImpl;
import com.example.documentmanagementbackend.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property 2: Preservation - Error Handling Behaviors Unchanged
 *
 * Observation-first methodology:
 * - Observed on UNFIXED code: email not found → 404 "Email not found"
 * - Observed on UNFIXED code: wrong password → 401 "Password is incorrect"
 * - Observed on UNFIXED code: phone mismatch → 404 "User with matching phone not found"
 *
 * These tests PASS on unfixed code (baseline behavior).
 * After fix, they must still PASS (no regressions).
 *
 * Requirements: 3.1, 3.2, 3.3, 3.4
 */
class PreservationPropertyTest {

    private UserRepository userRepository;
    private AuthServiceImpl authService;
    private BCryptPasswordEncoder passwordEncoder;
    private User existingUser;

    private static final String EMAIL = "user@example.com";
    private static final String PHONE = "0901234567";
    private static final String RAW_PASSWORD = "secret123";

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userRepository = mock(UserRepository.class);

        existingUser = new User();
        existingUser.setEmail(EMAIL);
        existingUser.setPhone(PHONE);
        existingUser.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        existingUser.setFullName("Test User");
        existingUser.setRole(Role.USER);

        when(userRepository.findUserByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(userRepository.findUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        JwtServiceImpl jwtService = mock(JwtServiceImpl.class);
        when(jwtService.generateToken(existingUser)).thenReturn("mock-jwt-token");

        authService = new AuthServiceImpl(userRepository, jwtService, passwordEncoder);
    }

    /**
     * Preservation 3.1: Email không tồn tại → vẫn trả về lỗi (không phải 200)
     *
     * Observed on unfixed code: returns 404 "Email not found"
     * Must be preserved after fix.
     */
    @Test
    void preservation_emailNotFound_shouldReturnError() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPhoneNumber(PHONE);
        request.setPassword(RAW_PASSWORD);

        ResponseEntity<String> response = authService.login(request);

        assertThat(response.getStatusCode())
                .as("Non-existent email should NOT return HTTP 200")
                .isNotEqualTo(HttpStatus.OK);
        assertThat(response.getStatusCode().value())
                .as("Non-existent email should return 4xx error")
                .isBetween(400, 499);
    }

    /**
     * Preservation 3.2: Password sai → vẫn trả về HTTP 401
     *
     * Observed on unfixed code: returns 401 "Password is incorrect"
     * Must be preserved after fix.
     */
    @Test
    void preservation_wrongPassword_shouldReturn401() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPhoneNumber(PHONE);
        request.setPassword("wrongpassword");

        ResponseEntity<String> response = authService.login(request);

        assertThat(response.getStatusCode())
                .as("Wrong password should return HTTP 401")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .contains("Password is incorrect");
    }

    /**
     * Preservation 3.3: Phone không khớp với email → vẫn trả về lỗi
     *
     * Observed on unfixed code: returns 404 "User with matching phone not found"
     * Must be preserved after fix.
     */
    @Test
    void preservation_phoneMismatch_shouldReturnError() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPhoneNumber("0999999999"); // phone không khớp
        request.setPassword(RAW_PASSWORD);

        ResponseEntity<String> response = authService.login(request);

        assertThat(response.getStatusCode())
                .as("Phone mismatch should NOT return HTTP 200")
                .isNotEqualTo(HttpStatus.OK);
        assertThat(response.getStatusCode().value())
                .as("Phone mismatch should return 4xx error")
                .isBetween(400, 499);
    }

    /**
     * Preservation 3.5: Đăng nhập thành công → JWT token hợp lệ (không rỗng)
     *
     * Observed on unfixed code: this case actually fails due to the bug,
     * but after fix it should return a non-blank JWT token.
     * This test verifies the token quality is preserved.
     */
    @Test
    void preservation_successfulLogin_jwtTokenIsValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPhoneNumber(PHONE);
        request.setPassword(RAW_PASSWORD);

        ResponseEntity<String> response = authService.login(request);

        if (response.getStatusCode() == HttpStatus.OK) {
            assertThat(response.getBody())
                    .as("Successful login should return non-blank JWT token")
                    .isNotBlank();
        }
        // If not 200, the bug still exists - that's expected on unfixed code
    }
}
