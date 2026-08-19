package com.example.demo.config.security;

import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Nhận biết request tới từ frontend React (fetch) để trả JSON,
     * ngược lại xử lý theo kiểu form HTML truyền thống (Thymeleaf).
     */
    private boolean wantsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String xhr = request.getHeader("X-Requested-With");
        return (accept != null && accept.contains("application/json")) || "XMLHttpRequest".equals(xhr);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // Cấu hình bắt buộc cho Spring Security 6 + SPA: 
        // Tắt tính năng "Trì hoãn CSRF" (Deferred CSRF) để cookie XSRF-TOKEN luôn được gửi về ngay.
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .csrf(csrf -> csrf
                // BƯỚC QUAN TRỌNG: Bỏ qua kiểm tra CSRF cho các API đăng nhập/đăng ký ban đầu
                // để tránh lỗi 403 do React chưa kịp có token khi gửi request lần đầu.
                .ignoringRequestMatchers("/login", "/register", "/api/auth/register")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
            )
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login", "/").permitAll()
                .requestMatchers("/admin/**").hasAuthority("admin")
                .requestMatchers("/dashboard/**").hasAuthority("user")
                .requestMatchers("/login", "/", "/register", "/register/success", "/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            // THÊM XỬ LÝ LỖI: Chặn Spring Security tự động redirect 302 nếu request lỗi thuộc về React
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (wantsJson(request)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        try {
                            objectMapper.writeValue(response.getWriter(), Map.of("error", "Vui lòng đăng nhập"));
                        } catch (Exception ignored) {}
                    } else {
                        try {
                            response.sendRedirect("/login");
                        } catch (Exception ignored) {}
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (wantsJson(request)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        try {
                            objectMapper.writeValue(response.getWriter(), Map.of("error", "Không có quyền truy cập hoặc lỗi CSRF"));
                        } catch (Exception ignored) {}
                    } else {
                        try {
                            response.sendRedirect("/login?error=access-denied");
                        } catch (Exception ignored) {}
                    }
                })
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    String role = authentication.getAuthorities().iterator().next().getAuthority();
                    String username = authentication.getName();

                    request.getSession().setAttribute("username", username);
                    request.getSession().setAttribute("role", role);

                    // Khắc phục lỗi: Lấy userId từ DB để lưu vào session
                    userRepository.findByUsername(username).ifPresent(user ->
                        request.getSession().setAttribute("userId", user.getId())
                    );

                    if (wantsJson(request)) {
                        response.setStatus(200);
                        response.setContentType("application/json;charset=UTF-8");
                        Map<String, Object> body = new HashMap<>();
                        body.put("username", username);
                        body.put("role", role);
                        objectMapper.writeValue(response.getWriter(), body);
                        return;
                    }

                    if ("admin".equals(role)) {
                        response.sendRedirect("/admin");
                    } else {
                        response.sendRedirect("/dashboard");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    if (wantsJson(request)) {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        objectMapper.writeValue(response.getWriter(),
                                Map.of("error", "Tài khoản hoặc mật khẩu không chính xác."));
                        return;
                    }
                    response.sendRedirect("/login?error=true");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    if (wantsJson(request)) {
                        response.setStatus(200);
                        response.setContentType("application/json;charset=UTF-8");
                        objectMapper.writeValue(response.getWriter(), Map.of("status", "SUCCESS"));
                        return;
                    }
                    response.sendRedirect("/login?logout=true");
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me", "XSRF-TOKEN")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Ép CsrfToken được "resolve" (và do đó lưu vào cookie XSRF-TOKEN) ngay ở MỌI request, kể cả
     * khi không có đoạn code nào (Thymeleaf, controller...) chủ động đọc CsrfToken.
     *
     * Lý do cần thiết: mặc định CookieCsrfTokenRepository dùng cơ chế "deferred" — token/cookie
     * chỉ thực sự được ghi khi có gì đó gọi CsrfToken.getToken(). Với các trang Thymeleaf cũ,
     * th:action tự động đọc _csrf nên cookie luôn được set. Nhưng SPA React không render HTML nào
     * ở server cả — nếu không ép resolve thủ công như dưới đây, request GET đầu tiên (vd
     * /api/auth/me lúc app vừa load) có thể không set cookie XSRF-TOKEN, khiến request POST/PUT
     * kế tiếp (login, transfer...) thiếu token để gửi lên. Đây là pattern chính thức được khuyến
     * nghị trong tài liệu Spring Security (mục "CSRF and Single Page Applications").
     */
    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                         FilterChain filterChain) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}