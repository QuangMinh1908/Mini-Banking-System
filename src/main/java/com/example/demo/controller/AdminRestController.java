package com.example.demo.controller;

import com.example.demo.dto.AccountDetailDTO;
import com.example.demo.dto.ResponseDTO;
import com.example.demo.dto.UserDetailDTO;
import com.example.demo.dto.UserListDTO;
import com.example.demo.dto.UserUpdateRequestDTO;
import com.example.demo.model.Account;
import com.example.demo.model.User;
import com.example.demo.model.UserUpdateRequest;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.UserUpdateRequestRepository;
import com.example.demo.service.AccountService;
import com.example.demo.service.UserListService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/admin/api")
public class AdminRestController {

    private final UserListService userService;
    private final AccountRepository accountRepository;
    private final UserUpdateRequestRepository requestRepository;
    private final AccountService accountService;

    public AdminRestController(UserListService userService, AccountRepository accountRepository,
                               UserUpdateRequestRepository requestRepository, AccountService accountService) {
        this.userService = userService;
        this.accountRepository = accountRepository;
        this.requestRepository = requestRepository;
        this.accountService = accountService;
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDetailDTO> getUserDetailsApi(@PathVariable Long id) {
        UserDetailDTO userDetailDTO = userService.getUserDetailById(id);
        if (userDetailDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDetailDTO);
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<UserUpdateRequestDTO>> getRequestsApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").ascending());
        Page<UserUpdateRequest> requests = requestRepository.findAll(pageable);
        Page<UserUpdateRequestDTO> dtoPage = requests.map(req -> new UserUpdateRequestDTO(
                req.getId(), req.getUser().getUsername(), req.getStatus(), req.getRequestDate(),
                req.getNewFullName(), req.getNewPhoneNumber(), req.getNewEmail(),
                req.getNewAddress(), req.getNewGender()
        ));
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/account/details/{accountNumber}")
    public ResponseEntity<AccountDetailDTO> getAccountBasicInfoApi(@PathVariable String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        User user = account.getUser();
        AccountDetailDTO dto = new AccountDetailDTO(
                account.getAccountNumber(), account.getDateOpen(), user.getId(),
                user.getFullName(), user.getPhoneNumber(), user.getEmail(),
                account.getAccountType().name(), account.getTransactionLimit(),
                account.getInterestRate(), account.getTermMonths()
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/user/{userId}/create-account")
    public ResponseEntity<ResponseDTO<String>> createAccountForUser(@PathVariable Long userId,
                                                                    @RequestBody Map<String, String> payload) {
        String accountType = payload.getOrDefault("accountType", "PAYMENT");
        String transactionLimit = payload.getOrDefault("transactionLimit", "50M");
        
        Integer termMonths = null;
        java.math.BigDecimal interestRate = null;
        
        // Nếu là tài khoản tiết kiệm thì mới lấy kỳ hạn và lãi suất
        if ("SAVING".equals(accountType)) {
            termMonths = payload.containsKey("termMonths") ? Integer.parseInt(payload.get("termMonths")) : 0;
            interestRate = payload.containsKey("interestRate") ? new java.math.BigDecimal(payload.get("interestRate")) : java.math.BigDecimal.ZERO;
        }
        
        // Gọi hàm Service đã được cập nhật
        Account newAccount = accountService.createNewAccountForUser(userId, accountType, transactionLimit, termMonths, interestRate);
        
        return ResponseEntity.ok(ResponseDTO.success("Cấp thành công tài khoản", newAccount.getAccountNumber()));
    }

    @GetMapping("/user/search")
    public ResponseEntity<ResponseDTO<List<UserListDTO>>> searchUserForWizard(@RequestParam String keyword) {
        keyword = keyword.trim();
        Page<UserListDTO> users;
        if (keyword.matches("\\d+")) {
            try {
                Long id = Long.parseLong(keyword);
                users = userService.searchUsers(id, null, null, 0, 5);
                if (users.isEmpty()) users = userService.searchUsers(null, null, keyword, 0, 5);
            } catch (NumberFormatException e) {
                users = userService.searchUsers(null, null, keyword, 0, 5);
            }
        } else {
            users = userService.searchUsers(null, keyword, null, 0, 5);
        }
        
        if (users.hasContent()) {
            return ResponseEntity.ok(ResponseDTO.success("Thành công", users.getContent()));
        } else {
            return ResponseEntity.status(404).body(ResponseDTO.error("Không tìm thấy khách hàng"));
        }
    }
}