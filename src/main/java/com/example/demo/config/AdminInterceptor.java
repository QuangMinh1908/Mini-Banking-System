package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");
        
        if (!"admin".equals(role)) {
            if ("user".equals(role)) {
                response.sendRedirect("/dashboard");
            } else {
                response.sendRedirect("/login");
            }
            return false;
        }
        
        return true;
    }
}