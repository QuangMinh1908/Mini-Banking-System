package com.example.demo.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final ConcurrentHashMap<String, Long> requestCounts = new ConcurrentHashMap<>();
    private static final long COOLDOWN_TIME = 1000;

    public RateLimitInterceptor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            requestCounts.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > COOLDOWN_TIME);
        }, 1, 1, TimeUnit.MINUTES);
    }

    // Lấy IP thật của client xuyên qua Load Balancer / Proxy
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equalsIgnoreCase("POST")) {

            String clientIp = getClientIp(request);
            String targetUri = request.getRequestURI();
            String key = clientIp + "-" + targetUri;
            
            long currentTime = System.currentTimeMillis();
            Long lastRequestTime = requestCounts.get(key);
            
            if (lastRequestTime != null && (currentTime - lastRequestTime) < COOLDOWN_TIME) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Bạn thao tác quá nhanh, vui lòng đợi 1 giây!\"}");
                return false;
            }
            
            requestCounts.put(key, currentTime);
        }
        return true;
    }
}