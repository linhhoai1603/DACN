package com.example.documentmanagementbackend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Họ tên là bắt buộc")
    String fullName;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    String email;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ")
    String phoneNumber;

    @NotBlank(message = "Password là bắt buộc")
    @Size(min = 6, max = 50, message = "Mật khẩu phải có từ 6 đến 50 ký tự")
    String password;

    String address;
}
