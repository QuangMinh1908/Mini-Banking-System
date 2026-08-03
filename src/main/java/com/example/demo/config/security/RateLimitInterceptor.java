package com.example.demo.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final ConcurrentHashMap<String, Long> userRequestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> ipRequestCounts = new ConcurrentHashMap<>();
    
    private static final long USER_COOLDOWN_TIME = 1000;
    private static final long IP_COOLDOWN_TIME = 50;
    public RateLimitInterceptor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            userRequestCounts.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > USER_COOLDOWN_TIME);
            ipRequestCounts.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > IP_COOLDOWN_TIME);
        }, 1, 1, TimeUnit.MINUTES);
    }

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr(); 
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equalsIgnoreCase("POST")) {
            long currentTime = System.currentTimeMillis();
            String targetUri = request.getRequestURI();
            
            // ==========================================
            // LỚP 1: KIỂM TRA THEO IP (Bảo vệ Hạ tầng)
            // ==========================================
            String clientIp = getClientIp(request);
            String ipKey = "IP-" + clientIp;
            Long lastIpRequestTime = ipRequestCounts.get(ipKey);
            
            if (lastIpRequestTime != null && (currentTime - lastIpRequestTime) < IP_COOLDOWN_TIME) {
                sendErrorResponse(response, "Hệ thống đang quá tải yêu cầu từ mạng của bạn. Vui lòng chậm lại!");
                return false;
            }
            ipRequestCounts.put(ipKey, currentTime);

            // ==========================================
            // LỚP 2: KIỂM TRA THEO USER_ID (Bảo vệ Nghiệp vụ)
            // ==========================================
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userId") != null) {
                Long userId = (Long) session.getAttribute("userId");
                String userKey = "USER-" + userId + "-" + targetUri;
                
                Long lastUserRequestTime = userRequestCounts.get(userKey);
                
                if (lastUserRequestTime != null && (currentTime - lastUserRequestTime) < USER_COOLDOWN_TIME) {
                    sendErrorResponse(response, "Bạn thao tác quá nhanh, vui lòng đợi 1 giây!");
                    return false;
                }
                userRequestCounts.put(userKey, currentTime);
            }
        }
        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}