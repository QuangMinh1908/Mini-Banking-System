package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * 1 dòng trong bảng "Quản lý Tài khoản" (trang Admin). CHỦ Ý dùng DTO thay vì trả thẳng entity
 * Account/User — entity User có field "password" (hash) không có @JsonIgnore, trả thẳng entity
 * qua JSON sẽ vô tình lộ password hash của khách hàng.
 */
public class AccountListDTO {
    private String accountNumber;
    private LocalDateTime dateOpen;
    private Owner user;

    public AccountListDTO(String accountNumber, LocalDateTime dateOpen, String ownerFullName) {
        this.accountNumber = accountNumber;
        this.dateOpen = dateOpen;
        this.user = new Owner(ownerFullName);
    }

    public String getAccountNumber() { return accountNumber; }
    public LocalDateTime getDateOpen() { return dateOpen; }
    public Owner getUser() { return user; }

    /** Lồng trong "user" để khớp shape mà frontend đang dùng: account.user.fullName */
    public static class Owner {
        private String fullName;

        public Owner(String fullName) {
            this.fullName = fullName;
        }

        public String getFullName() { return fullName; }
    }
}
