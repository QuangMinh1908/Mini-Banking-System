package com.example.demo.dto;

import java.util.List;

/**
 * Trả về cho GET /dashboard/api/transfer/accounts — dữ liệu chuẩn bị form Chuyển khoản
 * (thay cho model "sourceAccounts" + "allMyAccounts" mà TransferController render sẵn).
 */
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
