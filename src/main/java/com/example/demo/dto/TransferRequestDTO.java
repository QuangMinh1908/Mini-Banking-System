package com.example.demo.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TransferRequestDTO {

    @NotBlank(message = "Vui lòng chọn tài khoản nguồn")
    private String fromAccountNumber;

    @NotBlank(message = "Vui lòng nhập số tài khoản nhận tiền")
    @Pattern(regexp = "\\d{6,20}", message = "Số tài khoản đích không hợp lệ (chỉ gồm 6-20 chữ số)")
    private String toAccountNumber;

    @NotNull(message = "Vui lòng nhập số tiền cần chuyển")
    @Positive(message = "Số tiền phải lớn hơn 0")
    @Digits(integer = 17, fraction = 2, message = "Số tiền không hợp lệ")
    private BigDecimal amount;

    @Size(max = 255, message = "Nội dung giao dịch không được vượt quá 255 ký tự")
    private String description;

    public TransferRequestDTO() {
    }

    // --- Getters và Setters ---
    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }

    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}