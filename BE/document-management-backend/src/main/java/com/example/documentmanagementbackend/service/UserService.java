package com.example.documentmanagementbackend.service;

import com.example.documentmanagementbackend.dto.request.CreateUserRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {
    
    /**
     * Tạo người dùng mới (chỉ dành cho Admin)
     * @param request Thông tin người dùng cần tạo
     * @return ResponseEntity chứa thông tin người dùng đã tạo hoặc thông báo lỗi
     */
    ResponseEntity<?> createUser(CreateUserRequest request);
    
    /**
     * Kiểm tra email đã tồn tại chưa
     * @param email Email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    boolean existsByEmail(String email);
    
    /**
     * Kiểm tra số điện thoại đã tồn tại chưa
     * @param phone Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại đã tồn tại
     */
    boolean existsByPhone(String phone);
}
