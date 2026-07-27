package com.example.demo.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Bộ nhớ đệm lưu thời gian request cuối cùng của mỗi IP
    private final ConcurrentHashMap<String, Long> requestCounts = new ConcurrentHashMap<>();
    
    // Thời gian đợi bắt buộc giữa 2 lần click (2000ms = 2 giây)
    private static final long COOLDOWN_TIME = 2000; 

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Chỉ chống spam cho các hành động thay đổi dữ liệu (POST, PUT, DELETE)
        if (request.getMethod().equalsIgnoreCase("POST")) {
            
            // Lấy IP của người dùng và đường dẫn họ đang gọi
            String clientIp = request.getRemoteAddr();
            String targetUri = request.getRequestURI();
            String key = clientIp + "-" + targetUri;
            
            long currentTime = System.currentTimeMillis();
            Long lastRequestTime = requestCounts.get(key);

            // Nếu khoảng cách giữa 2 lần bấm chưa qua thời gian đợi -> Chặn!
            if (lastRequestTime != null && (currentTime - lastRequestTime) < COOLDOWN_TIME) {
                response.setStatus(429); // HTTP 429: Too Many Requests
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Bạn thao tác quá nhanh, vui lòng đợi vài giây!\"}");
                return false;
            }
            
            requestCounts.put(key, currentTime);
        }
        
        return true;
    }
}