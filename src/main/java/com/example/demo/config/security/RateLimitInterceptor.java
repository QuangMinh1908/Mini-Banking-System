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

    // Các endpoint GET mang tính "tra cứu" có thể bị lợi dụng để dò quét dữ liệu
    // (vd: dò số tài khoản để lấy họ tên chủ tài khoản) - trước đây interceptor chỉ áp
    // dụng cho POST nên các endpoint GET này hoàn toàn không bị giới hạn tốc độ.
    private static final java.util.Set<String> RATE_LIMITED_GET_PATHS = java.util.Set.of(
            "/api/transfer/lookup-receiver"
    );

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
            Long lastIpRequestTime = ipRequestCounts.get(ipKey);
            
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
                
                Long lastUserRequestTime = userRequestCounts.get(userKey);
                
                if (lastUserRequestTime != null && (currentTime - lastUserRequestTime) < USER_COOLDOWN_TIME) {
                    sendRateLimitResponse(request, response, "Bạn thao tác quá nhanh, vui lòng đợi 1 giây!");
                    return false;
                }
                userRequestCounts.put(userKey, currentTime);
            }
        }
        return true;
    }

    /**
     * Trước đây luôn trả JSON thô kể cả khi request là điều hướng trang bình thường
     * (submit form HTML, vd nút "Xác nhận chuyển khoản" bị double-click) - khiến trình
     * duyệt hiển thị nguyên văn {"error": "..."} thay vì quay lại trang với thông báo.
     * Giờ phân biệt 2 loại request:
     *  - Gọi bằng fetch()/AJAX (không ưu tiên text/html trong Accept header) -> vẫn trả JSON
     *    để tương thích với code JS hiện có (dashboard-transfer.js, common.js...).
     *  - Điều hướng trình duyệt thật sự (submit form, Accept ưu tiên text/html) -> redirect
     *    (303) về lại trang trước đó kèm cờ để hiển thị thông báo thân thiện, thay vì JSON thô.
     */
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