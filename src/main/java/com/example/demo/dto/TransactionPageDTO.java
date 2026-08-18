package com.example.demo.dto;

import java.util.List;

/**
 * Trả về cho GET /dashboard/api/transactions — thay cho fragment HTML "dashboard :: txItems" /
 * "dashboard-history :: txItems" mà DashboardController.loadMoreTransactions() render trước đây.
 * hasMore = true nếu số lượng item trả về bằng đúng "size" yêu cầu (khả năng còn trang kế tiếp).
 */
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
