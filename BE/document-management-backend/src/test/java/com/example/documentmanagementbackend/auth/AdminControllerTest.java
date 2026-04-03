package com.example.documentmanagementbackend.auth;

import com.example.documentmanagementbackend.dto.request.CreateUserRequest;
import com.example.documentmanagementbackend.dto.response.CreateUserResponse;
import com.example.documentmanagementbackend.model.Role;
import com.example.documentmanagementbackend.model.User;
import com.example.documentmanagementbackend.repository.UserRepository;
import com.example.documentmanagementbackend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateUserRequest();
        validRequest.setFullName("Nguyễn Văn A");
        validRequest.setEmail("test@example.com");
        validRequest.setPhone("0901234567");
        validRequest.setPassword("password123");
        validRequest.setAddress("Hà Nội");
        validRequest.setRole(Role.USERS);
    }

    @Test
    @DisplayName("Tạo user thành công khi email và phone chưa tồn tại")
    void createUser_WithValidData_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName("Nguyễn Văn A");
        savedUser.setEmail("test@example.com");
        savedUser.setPhone("0901234567");
        savedUser.setAddress("Hà Nội");
        savedUser.setRole(Role.USERS);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        ResponseEntity<?> response = userService.createUser(validRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof CreateUserResponse);

        CreateUserResponse body = (CreateUserResponse) response.getBody();
        assertEquals(1L, body.getId());
        assertEquals("Nguyễn Văn A", body.getFullName());
        assertEquals("test@example.com", body.getEmail());
        assertEquals("Tạo người dùng thành công", body.getMessage());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Tạo user thất bại khi email đã tồn tại")
    void createUser_EmailExists_Conflict() {
        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());

        ResponseEntity<?> response = userService.createUser(validRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals("Email đã tồn tại trong hệ thống", errors.get("email"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Tạo user thất bại khi phone đã tồn tại")
    void createUser_PhoneExists_Conflict() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        User existingUser = new User();
        existingUser.setPhone("0901234567");
        when(userRepository.findByPhone("0901234567")).thenReturn(Optional.of(existingUser));

        ResponseEntity<?> response = userService.createUser(validRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals("Số điện thoại đã tồn tại trong hệ thống", errors.get("phone"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Tạo user thất bại khi cả email và phone đã tồn tại")
    void createUser_EmailAndPhoneExist_Conflict() {
        User existingUserByEmail = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUserByEmail));

        User existingUserByPhone = new User();
        when(userRepository.findByPhone("0901234567")).thenReturn(Optional.of(existingUserByPhone));

        ResponseEntity<?> response = userService.createUser(validRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(2, errors.size());
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("phone"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Tạo user với role ADMIN thành công")
    void createUser_WithAdminRole_Success() {
        validRequest.setRole(Role.ADMIN);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setRole(Role.ADMIN);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        ResponseEntity<?> response = userService.createUser(validRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        CreateUserResponse body = (CreateUserResponse) response.getBody();
        assertEquals(Role.ADMIN, body.getRole());
    }

    @Test
    @DisplayName("Kiểm tra existsByEmail trả về true khi email tồn tại")
    void existsByEmail_ReturnsTrue() {
        User existingUser = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        assertTrue(userService.existsByEmail("test@example.com"));
    }

    @Test
    @DisplayName("Kiểm tra existsByEmail trả về false khi email chưa tồn tại")
    void existsByEmail_ReturnsFalse() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        assertFalse(userService.existsByEmail("new@example.com"));
    }

    @Test
    @DisplayName("Kiểm tra existsByPhone trả về true khi phone tồn tại")
    void existsByPhone_ReturnsTrue() {
        User existingUser = new User();
        when(userRepository.findByPhone("0901234567")).thenReturn(Optional.of(existingUser));

        assertTrue(userService.existsByPhone("0901234567"));
    }

    @Test
    @DisplayName("Kiểm tra existsByPhone trả về false khi phone chưa tồn tại")
    void existsByPhone_ReturnsFalse() {
        when(userRepository.findByPhone("0909999999")).thenReturn(Optional.empty());

        assertFalse(userService.existsByPhone("0909999999"));
    }
}
