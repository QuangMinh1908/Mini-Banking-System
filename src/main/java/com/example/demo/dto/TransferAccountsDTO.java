package com.example.demo.dto;

import java.util.List;

public class TransferAccountsDTO {
    private List<DashboardAccountDTO> sourceAccounts;
    private List<DashboardAccountDTO> allMyAccounts;

    public TransferAccountsDTO(List<DashboardAccountDTO> sourceAccounts, List<DashboardAccountDTO> allMyAccounts) {
        this.sourceAccounts = sourceAccounts;
        this.allMyAccounts = allMyAccounts;
    }

    public List<DashboardAccountDTO> getSourceAccounts() { return sourceAccounts; }
    public List<DashboardAccountDTO> getAllMyAccounts() { return allMyAccounts; }
}
