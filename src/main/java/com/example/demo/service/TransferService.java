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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction transferMoney(Long currentUserId, TransferRequestDTO request) {
        String fromAccountNumber = request.getFromAccountNumber().trim();
        String toAccountNumber = request.getToAccountNumber().trim();

        // 1. KIỂM TRA LOGIC CƠ BẢN ĐẦU TIÊN
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidTransferException("Không thể chuyển tiền đến chính tài khoản nguồn!");
        }

        // 2. CHỐNG DEADLOCK: SẮP XẾP THỨ TỰ KHÓA TÀI KHOẢN (Quy tắc vàng)
        String firstLockAcc;
        String secondLockAcc;

        if (fromAccountNumber.compareTo(toAccountNumber) < 0) {
            firstLockAcc = fromAccountNumber;
            secondLockAcc = toAccountNumber;
        } else {
            firstLockAcc = toAccountNumber;
            secondLockAcc = fromAccountNumber;
        }

        Account account1 = accountRepository.findByAccountNumberForUpdate(firstLockAcc);
        Account account2 = accountRepository.findByAccountNumberForUpdate(secondLockAcc);

        if (account1 == null || account2 == null) {
            throw new AccountNotFoundException("Không tìm thấy tài khoản nguồn hoặc đích! Vui lòng kiểm tra lại.");
        }

        // 3. PHÂN ĐỊNH LẠI ĐÂU LÀ TÀI KHOẢN GỬI, ĐÂU LÀ TÀI KHOẢN NHẬN
        Account fromAccount = account1.getAccountNumber().equals(fromAccountNumber) ? account1 : account2;
        Account toAccount = account1.getAccountNumber().equals(toAccountNumber) ? account1 : account2;

        // 4. XÁC THỰC CHỦ SỞ HỮU & LOẠI TÀI KHOẢN NGUỒN
        if (fromAccount.getUser() == null || !fromAccount.getUser().getId().equals(currentUserId)) {
            throw new AccountNotFoundException("Tài khoản nguồn không hợp lệ hoặc không thuộc quyền sở hữu của bạn!");
        }
        if (fromAccount.getAccountType() != AccountType.PAYMENT) {
            throw new InvalidTransferException("Giao dịch thất bại! Chỉ được phép chuyển tiền từ tài khoản thanh toán.");
        }

        // 4b. XÁC THỰC LOẠI TÀI KHOẢN ĐÍCH: tài khoản tiết kiệm (SAVING) là sổ kỳ hạn riêng
        // của từng chủ sở hữu, KHÔNG cho phép người khác chuyển thẳng tiền vào (chỉ chủ sổ
        // được tự nạp thêm vào chính sổ của mình từ tài khoản thanh toán).
        if (toAccount.getAccountType() == AccountType.SAVING
                && (toAccount.getUser() == null || !toAccount.getUser().getId().equals(currentUserId))) {
            throw new InvalidTransferException("Không thể chuyển tiền vào tài khoản tiết kiệm của người khác!");
        }

        // 5. KIỂM TRA HẠN MỨC GIAO DỊCH
        BigDecimal amount = request.getAmount();
        String limitStr = fromAccount.getTransactionLimit();

        if (limitStr != null && !"UNLIMITED".equals(limitStr)) {
            BigDecimal maxLimit = resolveMaxLimit(limitStr, fromAccount.getAccountNumber());

            if (amount.compareTo(maxLimit) > 0) {
                throw new InvalidTransferException("Số tiền vượt quá hạn mức (" + limitStr + ") trên 1 giao dịch!");
            }

            LocalDateTime startOfDay = LocalDateTime.now().with(java.time.LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.now().with(java.time.LocalTime.MAX);
            BigDecimal totalSpentToday = transactionRepository.sumOutgoingAmountByAccountIdAndDate(fromAccount.getId(), startOfDay, endOfDay);

            if (totalSpentToday.add(amount).compareTo(maxLimit) > 0) {
                throw new InvalidTransferException("Giao dịch thất bại! Tổng số tiền giao dịch trong ngày của bạn đã vượt quá hạn mức " + limitStr);
            }
        }

        // 6. KIỂM TRA SỐ DƯ
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Số dư tài khoản nguồn không đủ để thực hiện giao dịch này!");
        }

        // 7. THỰC THI: trừ tiền nguồn -> cộng tiền đích -> lưu cả
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 8. GHI NHẬN LỊCH SỬ GIAO DỊCH (BÚT TOÁN KÉP)
        String sharedTransactionId = generateTransactionId();
        LocalDateTime now = LocalDateTime.now();
        String reqDescription = request.getDescription();
        
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

        Transaction creditTx = new Transaction();
        creditTx.setTransactionId(sharedTransactionId);
        creditTx.setType(TransactionType.TRANSFER);
        creditTx.setAccount(toAccount);
        creditTx.setRelatedAccountNumber(fromAccount.getAccountNumber());
        creditTx.setAmount(amount);
        creditTx.setDirection("CREDIT");
        creditTx.setTransactionDate(now);
        creditTx.setDescription((reqDescription == null || reqDescription.isBlank()) 
                ? "Nhận tiền từ " + fromAccount.getUser().getFullName() 
                : reqDescription.trim());

        transactionRepository.save(debitTx);
        transactionRepository.save(creditTx);

        // Log audit: phục vụ tra soát/đối chiếu giao dịch sau này (không log ở mức DEBUG vì
        // đây là dữ liệu tài chính cần giữ lại theo mức log mặc định INFO của production).
        logger.info("Transfer succeeded - txId={}, fromAcc={}, toAcc={}, amount={}, userId={}",
                sharedTransactionId, fromAccount.getAccountNumber(), toAccount.getAccountNumber(),
                amount, currentUserId);

        return debitTx;
    }

    /**
     * Quy đổi mã hạn mức (lưu dạng String trong DB) sang số tiền tối đa.
     * FAIL-CLOSED: nếu gặp giá trị không nằm trong danh sách đã biết (dữ liệu lỗi do
     * migration/nhập tay/bug ở nơi khác), TỪ CHỐI giao dịch thay vì âm thầm bỏ qua kiểm tra
     * hạn mức như code cũ (fail-open) - tránh vô tình cho phép chuyển không giới hạn.
     */
    private BigDecimal resolveMaxLimit(String limitStr, String accountNumber) {
        switch (limitStr) {
            case "50M":
                return new BigDecimal("50000000");
            case "500M":
                return new BigDecimal("500000000");
            default:
                logger.error("Phát hiện transactionLimit không hợp lệ trong dữ liệu: account={}, value={}",
                        accountNumber, limitStr);
                throw new InvalidTransferException(
                        "Không thể xác định hạn mức giao dịch của tài khoản nguồn. Vui lòng liên hệ hỗ trợ để được kiểm tra lại!");
        }
    }

    private String generateTransactionId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return "TXN-" + datePart + "-" + randomPart;
    }
}