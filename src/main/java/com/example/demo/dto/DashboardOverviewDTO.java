package com.example.demo.dto;

import java.util.List;

/**
 * Trả về cho GET /dashboard/api/overview — tổng quan trang Dashboard (thay cho model
 * "username" + "accounts" mà DashboardController render sẵn cho Thymeleaf).
 */
public class DashboardOverviewDTO {
    private String username;
    private List<DashboardAccountDTO> accounts;

    public DashboardOverviewDTO(String username, List<DashboardAccountDTO> accounts) {
        this.username = username;
        this.accounts = accounts;
    }

    public String getUsername() { return username; }
    public List<DashboardAccountDTO> getAccounts() { return accounts; }
}
