package com.example.demo.service;

import com.example.demo.dto.TransferRequestDTO;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.exception.InvalidTransferException;
import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.model.enums.TransactionType;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.model.enums.AccountType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction transferMoney(Long currentUserId, TransferRequestDTO request) {

        // 1. XÁC THỰC TÀI KHOẢN NGUỒN
        String fromAccountNumber = request.getFromAccountNumber().trim();
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber);

        if (fromAccount == null || fromAccount.getUser() == null
                || !fromAccount.getUser().getId().equals(currentUserId)) {
            throw new AccountNotFoundException("Tài khoản nguồn không hợp lệ hoặc không thuộc quyền sở hữu của bạn!");
        }
        if (fromAccount.getAccountType() != AccountType.PAYMENT) {
            throw new InvalidTransferException("Giao dịch thất bại! Chỉ được phép chuyển tiền từ tài khoản thanh toán.");
        }

        // 2. XÁC THỰC TÀI KHOẢN ĐÍCH
        String toAccountNumber = request.getToAccountNumber().trim();
        Account toAccount = accountRepository.findByAccountNumberForUpdate(toAccountNumber);

        if (toAccount == null) {
            throw new AccountNotFoundException("Không tìm thấy tài khoản đích! Vui lòng kiểm tra lại số tài khoản.");
        }

        // 3. KIỂM TRA LOGIC
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new InvalidTransferException("Không thể chuyển tiền đến chính tài khoản nguồn!");
        }
        BigDecimal amount = request.getAmount();
        String limitStr = fromAccount.getTransactionLimit();
        
        if (limitStr != null && !"UNLIMITED".equals(limitStr)) {
            BigDecimal maxLimit = BigDecimal.ZERO;
            if ("50M".equals(limitStr)) {
                maxLimit = new BigDecimal("50000000");
            } else if ("500M".equals(limitStr)) {
                maxLimit = new BigDecimal("500000000");
            }

            if (maxLimit.compareTo(BigDecimal.ZERO) > 0) {
                // Kiểm tra: Số tiền 1 giao dịch có vượt hạn mức không
                if (amount.compareTo(maxLimit) > 0) {
                    throw new InvalidTransferException("Số tiền vượt quá hạn mức (" + limitStr + ") trên 1 giao dịch!");
                }

                // Kiểm tra: Tổng số tiền giao dịch trong ngày có vượt hạn mức không
                LocalDateTime startOfDay = LocalDateTime.now().with(java.time.LocalTime.MIN);
                LocalDateTime endOfDay = LocalDateTime.now().with(java.time.LocalTime.MAX);
                BigDecimal totalSpentToday = transactionRepository.sumOutgoingAmountByAccountIdAndDate(fromAccount.getId(), startOfDay, endOfDay);

                if (totalSpentToday.add(amount).compareTo(maxLimit) > 0) {
                    throw new InvalidTransferException("Giao dịch thất bại! Tổng số tiền giao dịch trong ngày của bạn đã vượt quá hạn mức " + limitStr);
                }
            }
        }

        // 4. KIỂM TRA SỐ DƯ
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Số dư tài khoản nguồn không đủ để thực hiện giao dịch này!");
        }

        // 5. THỰC THI: trừ tiền nguồn -> cộng tiền đích -> lưu cả
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 6. GHI NHẬN LỊCH SỬ GIAO DỊCH (BÚT TOÁN KÉP)
        String sharedTransactionId = generateTransactionId();
        LocalDateTime now = LocalDateTime.now();
        String reqDescription = request.getDescription();
        
        // Record 1: DEBIT (Trừ tiền người gửi)
        Transaction debitTx = new Transaction();
        debitTx.setTransactionId(sharedTransactionId);
        debitTx.setType(TransactionType.TRANSFER);
        debitTx.setAccount(fromAccount);
        debitTx.setRelatedAccountNumber(toAccount.getAccountNumber());
        debitTx.setAmount(amount);
        debitTx.setDirection("DEBIT");
        debitTx.setTransactionDate(now);
        debitTx.setDescription((reqDescription == null || reqDescription.isBlank()) 
                ? "Chuyển tiền đến " + toAccount.getUser().getFullName() 
                : reqDescription.trim());

        // Record 2: CREDIT (Cộng tiền người nhận)
        Transaction creditTx = new Transaction();
        creditTx.setTransactionId(sharedTransactionId);
        creditTx.setType(TransactionType.TRANSFER);
        creditTx.setAccount(toAccount);
        creditTx.setRelatedAccountNumber(fromAccount.getAccountNumber());
        creditTx.setAmount(amount);
        creditTx.setDirection("CREDIT");
        creditTx.setTransactionDate(now);
        creditTx.setDescription("Nhận tiền từ " + fromAccount.getUser().getFullName());

        transactionRepository.save(debitTx);
        transactionRepository.save(creditTx);

        return debitTx;
    }
/**
     * Sinh mã giao dịch duy nhất theo định dạng TXN-yyyyMMdd-XXXXXXXXXXXXXXXX, 
     * mở rộng phần random lên 16 ký tự để triệt tiêu hoàn toàn nguy cơ trùng lặp.
     */
    private String generateTransactionId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return "TXN-" + datePart + "-" + randomPart;
    }
}