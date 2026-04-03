package com.example.documentmanagementbackend.security;

import com.example.documentmanagementbackend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Inject qua Constructor thay vì @Autowired (Clean Code)
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy header Authorization từ request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Kiểm tra xem header có chứa token hay không (phải bắt đầu bằng "Bearer ")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Nếu không có token, cho request đi tiếp (có thể bị chặn lại ở các filter bảo mật phía sau nếu endpoint cần xác thực)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Cắt chuỗi để lấy token thực sự (bỏ qua "Bearer " dài 7 ký tự)
        jwt = authHeader.substring(7);

        // 4. Lấy username từ chuỗi JWT (JwtService sẽ kiểm tra tính toàn vẹn và ném lỗi nếu token bị can thiệp)
        username = jwtService.extractUsername(jwt);

        // 5. Nếu lấy được username và trong SecurityContext chưa có phiên đăng nhập nào
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Query Database lấy thông tin user lên
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 7. Xác nhận lại token xem có trùng khớp với userDetails và còn hạn không
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 8. Tạo đối tượng Authentication để báo cho Spring Security biết user này hợp lệ
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Thông thường không lưu credential (password) ở đây
                        userDetails.getAuthorities()
                );

                // Lưu thêm các chi tiết của Request (như IP, SessionId...)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. Cập nhật SecurityContextHolder (thực hiện "đăng nhập" vào context)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Chuyển tiếp request tới filter/controller tiếp theo
        filterChain.doFilter(request, response);
    }
}
