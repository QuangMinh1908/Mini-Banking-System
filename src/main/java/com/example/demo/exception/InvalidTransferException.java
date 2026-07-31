package com.example.demo.exception;
/**
 * Ném ra khi yêu cầu chuyển khoản vi phạm một quy tắc nghiệp vụ
 * (không tính đến việc tài khoản không tồn tại hoặc không đủ số dư,
 * vốn đã có 2 exception riêng: AccountNotFoundException, InsufficientBalanceException).
 * Ví dụ: chuyển tiền đến chính tài khoản nguồn.
 */
public class InvalidTransferException extends RuntimeException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
