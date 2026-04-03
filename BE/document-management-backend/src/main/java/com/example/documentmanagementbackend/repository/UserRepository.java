package com.example.documentmanagementbackend.repository;

import com.example.documentmanagementbackend.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findUserByEmail(@NotBlank(message = "Email là bắt buộc") @Email(message = "Email không hợp lệ") String email);
}
