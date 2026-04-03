package com.example.documentmanagementbackend.dto.response;

import com.example.documentmanagementbackend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Role role;
    private String message;
}
