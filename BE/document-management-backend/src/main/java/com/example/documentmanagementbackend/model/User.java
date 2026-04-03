package com.example.documentmanagementbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Entity
@Table(name = "users")
// Đổi tên bảng thành "users" vì "user" thường là từ khóa từ khóa hệ thống trong nhiều database (như PostgreSQL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "address")
    private String address;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    // Đánh dấu unique vì số điện thoại thường dùng để định danh/đăng nhập
    @Column(name = "phone", unique = true, nullable = false, length = 15)
    private String phone;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // --- CÁC METHOD CỦA INTERFACE USERDETAILS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Trả về danh sách quyền của user. Spring Security mặc định hiểu prefix "ROLE_"
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // Trả về trường được dùng làm định danh đăng nhập chính.
        // Ở đây giả sử dùng số điện thoại làm định danh. Nếu hệ thống dùng cả username,
        // bạn có thể thêm trường username vào Entity và return nó ở đây.
        return phone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Trả về true nếu tài khoản không bị hết hạn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Trả về true nếu tài khoản không bị khóa
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Trả về true nếu mật khẩu không bị hết hạn
    }

    @Override
    public boolean isEnabled() {
        return true; // Trả về true nếu tài khoản đang được kích hoạt (có thể map với 1 trường isActive trong DB)
    }
}
