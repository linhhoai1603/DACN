package com.example.documentmanagementbackend.auth;

import com.example.documentmanagementbackend.dto.request.LoginRequest;
import com.example.documentmanagementbackend.model.Role;
import com.example.documentmanagementbackend.model.User;
import com.example.documentmanagementbackend.repository.UserRepository;
import com.example.documentmanagementbackend.service.UserRepositoryDetailsService;
import com.example.documentmanagementbackend.service.impl.AuthServiceImpl;
import com.example.documentmanagementbackend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property 1: Bug Condition - getUsername() Returns Phone Instead of Email
 *
 * GOAL: Surface counterexamples that demonstrate the bug exists.
 * Run on UNFIXED code - tests WILL FAIL (this confirms the bug exists).
 *
 * Bug Condition: User.getUsername() returns phone, but loadUserByUsername()
 * uses findByEmail() → mismatch causes UsernameNotFoundException on valid login.
 */
class BugConditionExplorationTest {

    private User user;
    private UserRepository userRepository;
    private UserRepositoryDetailsService userDetailsService;
    private AuthServiceImpl authService;
    private BCryptPasswordEncoder passwordEncoder;

    private static final String EMAIL = "user@example.com";
    private static final String PHONE = "0901234567";
    private static final String RAW_PASSWORD = "secret123";

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userRepository = mock(UserRepository.class);

        user = new User();
        user.setEmail(EMAIL);
        user.setPhone(PHONE);
        user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        user.setFullName("Test User");
        user.setRole(Role.USER);

        when(userRepository.findUserByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        // phone lookup should NOT find user (findByEmail with phone value)
        when(userRepository.findUserByEmail(PHONE)).thenReturn(Optional.empty());

        userDetailsService = new UserRepositoryDetailsService();
        // inject mock via reflection since @Autowired
        try {
            var field = UserRepositoryDetailsService.class.getDeclaredField("repository");
            field.setAccessible(true);
            field.set(userDetailsService, userRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JwtService jwtService = mock(JwtService.class);
        when(jwtService.generateToken(user)).thenReturn("mock-jwt-token");

        authService = new AuthServiceImpl(userRepository, jwtService, passwordEncoder);
    }

    /**
     * Property 1 - Bug Condition Check:
     * User.getUsername() MUST return email (not phone) to be consistent with loadUserByUsername().
     *
     * EXPECTED ON UNFIXED CODE: FAILS - getUsername() returns phone "0901234567"
     * EXPECTED AFTER FIX: PASSES - getUsername() returns email "user@example.com"
     *
     * Counterexample documented: user.getUsername() = "0901234567" instead of "user@example.com"
     */
    @Test
    void property1_getUsername_shouldReturnEmail_notPhone() {
        // Bug condition: getUsername() returns phone instead of email
        // This test FAILS on unfixed code (returns "0901234567" instead of "user@example.com")
        assertThat(user.getUsername())
                .as("User.getUsername() should return email to match loadUserByUsername(findByEmail)")
                .isEqualTo(EMAIL);
    }

    /**
     * Property 1 - Bug Condition Check:
     * loadUserByUsername(phone) should throw UsernameNotFoundException
     * because loadUserByUsername uses findByEmail, and phone is not an email.
     *
     * This confirms the mismatch: Spring Security calls loadUserByUsername(getUsername())
     * which passes phone to findByEmail → not found.
     */
    @Test
    void property1_loadUserByUsername_withPhone_shouldThrow() {
        // Confirms the bug: if getUsername() returns phone, Spring Security will call
        // loadUserByUsername(phone) → findByEmail(phone) → UsernameNotFoundException
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(PHONE))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    /**
     * Property 1 - Bug Condition Check (main scenario):
     * login() with valid email + phone + password MUST return HTTP 200 + JWT.
     *
     * EXPECTED ON UNFIXED CODE: FAILS - returns HTTP 404 "Email not found"
     * EXPECTED AFTER FIX: PASSES - returns HTTP 200 + JWT token
     *
     * Counterexample: login(valid credentials) → 404 instead of 200
     */
    @Test
    void property1_login_withValidCredentials_shouldReturn200() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPhoneNumber(PHONE);
        request.setPassword(RAW_PASSWORD);

        ResponseEntity<String> response = authService.login(request);

        assertThat(response.getStatusCode())
                .as("Valid login should return HTTP 200, not 404")
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .as("Response body should contain JWT token")
                .isNotBlank();
    }
}
