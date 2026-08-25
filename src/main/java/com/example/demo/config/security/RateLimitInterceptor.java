package com.example.demo.config.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;
import java.util.Set;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static final long USER_COOLDOWN_TIME = 1000;
    private static final long IP_COOLDOWN_TIME = 50;
    private static final Set<String> RATE_LIMITED_GET_PATHS = Set.of("/api/transfer/lookup-receiver");

    // BỘ ĐỆM IP: Tối đa 10,000 IP, tự động dọn dẹp sau 1 phút không truy cập (Chống OOM)
    private final Cache<String, Long> ipRequestCounts = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    // BỘ ĐỆM USER: Tối đa 10,000 User, tự động dọn dẹp sau 1 phút không truy cập (Chống OOM)
    private final Cache<String, Long> userRequestCounts = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr(); 
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String targetUri = request.getRequestURI();
        boolean isPost = request.getMethod().equalsIgnoreCase("POST");
        boolean isSensitiveGet = request.getMethod().equalsIgnoreCase("GET") && RATE_LIMITED_GET_PATHS.contains(targetUri);

        if (isPost || isSensitiveGet) {
            long currentTime = System.currentTimeMillis();
            
            // ==========================================
            // LỚP 1: KIỂM TRA THEO IP (Bảo vệ Hạ tầng)
            // ==========================================
            String clientIp = getClientIp(request);
            String ipKey = "IP-" + clientIp;
            Long lastIpRequestTime = ipRequestCounts.getIfPresent(ipKey);
            
            if (lastIpRequestTime != null && (currentTime - lastIpRequestTime) < IP_COOLDOWN_TIME) {
                sendRateLimitResponse(request, response, "Hệ thống đang quá tải yêu cầu từ mạng của bạn. Vui lòng chậm lại!");
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
                
                Long lastUserRequestTime = userRequestCounts.getIfPresent(userKey);
                
                if (lastUserRequestTime != null && (currentTime - lastUserRequestTime) < USER_COOLDOWN_TIME) {
                    sendRateLimitResponse(request, response, "Bạn thao tác quá nhanh, vui lòng đợi 1 giây!");
                    return false;
                }
                userRequestCounts.put(userKey, currentTime);
            }
        }
        return true;
    }

    private void sendRateLimitResponse(HttpServletRequest request, HttpServletResponse response, String message) throws Exception {
        String acceptHeader = request.getHeader("Accept");
        boolean looksLikeBrowserNavigation = acceptHeader != null && acceptHeader.contains("text/html");

        if (looksLikeBrowserNavigation) {
            String referer = request.getHeader("Referer");
            String redirectTarget = (referer != null && !referer.isBlank()) ? referer : "/dashboard";
            String separator = redirectTarget.contains("?") ? "&" : "?";
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            response.setHeader("Location", redirectTarget + separator + "rateLimited=true");
        } else {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"" + message + "\"}");
        }
    }
}