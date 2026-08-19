package com.example.demo.dto;

import java.util.List;

public class TransactionPageDTO {
    private List<DashboardTransactionDTO> transactions;
    private boolean hasMore;

    public TransactionPageDTO(List<DashboardTransactionDTO> transactions, boolean hasMore) {
        this.transactions = transactions;
        this.hasMore = hasMore;
    }

    public List<DashboardTransactionDTO> getTransactions() { return transactions; }
    public boolean isHasMore() { return hasMore; }
}
