package com.example.demo.controller;

import com.example.demo.dto.DashboardAccountDTO;
import com.example.demo.dto.DashboardOverviewDTO;
import com.example.demo.dto.DashboardTransactionDTO;
import com.example.demo.dto.TransactionPageDTO;
import com.example.demo.dto.TransferAccountsDTO;
import com.example.demo.dto.TransferRequestDTO;
import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.model.enums.AccountType;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.service.AccountListService;
import com.example.demo.service.TransactionSpecification;
import com.example.demo.service.TransferService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/dashboard/api")
public class DashboardRestController {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransferService transferService;

    public DashboardRestController(TransactionRepository transactionRepository,
                                    AccountRepository accountRepository, TransferService transferService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transferService = transferService;
    }

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDTO> overview(HttpSession session) {
        Long currentUserId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");

        List<DashboardAccountDTO> accountDTOs = loadAllAccounts(currentUserId);
        return ResponseEntity.ok(new DashboardOverviewDTO(username, accountDTOs));
    }

    @GetMapping("/transactions")
    public ResponseEntity<TransactionPageDTO> transactions(
            HttpSession session,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastDate,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dashboard") String source) {

        Long currentUserId = (Long) session.getAttribute("userId");

        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "transactionDate", "id"));
        Page<Transaction> txPage = transactionRepository.findAll(
                TransactionSpecification.isRelatedToUserIdWithCursor(currentUserId, lastDate, lastId), pageable);

        List<DashboardTransactionDTO> transactionDTOs = mapTransactionsToDTOs(txPage);
        boolean hasMore = transactionDTOs.size() == size;

        return ResponseEntity.ok(new TransactionPageDTO(transactionDTOs, hasMore));
    }

    @GetMapping("/transfer/accounts")
    public ResponseEntity<TransferAccountsDTO> transferAccounts(HttpSession session) {
        Long currentUserId = (Long) session.getAttribute("userId");

        List<DashboardAccountDTO> allAccounts = loadAllAccounts(currentUserId);
        List<DashboardAccountDTO> sourceAccounts = allAccounts.stream()
                .filter(acc -> acc.getAccountType() == AccountType.PAYMENT)
                .toList();

        return ResponseEntity.ok(new TransferAccountsDTO(sourceAccounts, allAccounts));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequestDTO transferRequest,
                                                          HttpSession session) {
        Long currentUserId = (Long) session.getAttribute("userId");
        Transaction result = transferService.transferMoney(currentUserId, transferRequest);
        return ResponseEntity.ok(Map.of("transactionId", result.getTransactionId()));
    }

    private List<DashboardAccountDTO> loadAllAccounts(Long userId) {
        List<Account> accounts = accountRepository.findAll(AccountListService.hasUserId(userId));
        return accounts.stream()
                .map(acc -> new DashboardAccountDTO(
                        acc.getAccountNumber(),
                        acc.getAccountType(),
                        acc.getTransactionLimit(),
                        acc.getBalance(),
                        acc.getDateOpen(),
                        acc.getInterestRate(),
                        acc.getTermMonths()))
                .toList();
    }

    private List<DashboardTransactionDTO> mapTransactionsToDTOs(Page<Transaction> txPage) {
        return txPage.getContent().stream()
                .map(tx -> new DashboardTransactionDTO(
                        tx.getId(),
                        tx.getTransactionId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getTransactionDate(),
                        tx.getDirection(),
                        tx.getAccount().getAccountNumber()))
                .toList();
    }
}
