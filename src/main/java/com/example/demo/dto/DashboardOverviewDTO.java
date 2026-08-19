package com.example.demo.dto;

import java.util.List;

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
