package com.example.documentmanagementbackend.dto.request;

import com.example.documentmanagementbackend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Họ tên là bắt buộc")
    @Size(min = 2, max = 100, message = "Họ tên phải có từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$",
            message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 6, max = 50, message = "Mật khẩu phải có từ 6 đến 50 ký tự")
    private String password;

    private String address;

    @NotNull(message = "Role là bắt buộc")
    private Role role;
}
