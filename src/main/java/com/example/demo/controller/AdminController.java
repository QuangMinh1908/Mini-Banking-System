package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserListService;
import com.example.demo.service.AccountListService;
import com.example.demo.config.security.UserListDTO;
import com.example.demo.config.security.UserUpdateRequestDTO;
import com.example.demo.config.security.UserDetailDTO;
import com.example.demo.repository.UserUpdateRequestRepository;
import com.example.demo.model.UserUpdateRequest;
import com.example.demo.config.security.AccountDetailDTO;
import com.example.demo.util.AccountUtils;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Controller
public class AdminController {

    private final UserListService userService;
    private final AccountRepository accountRepository;
    private final UserUpdateRequestRepository requestRepository;
    private final UserRepository userRepository;

    public AdminController(UserListService userService, AccountRepository accountRepository,
                            UserUpdateRequestRepository requestRepository, UserRepository userRepository) {
        this.userService = userService;
        this.accountRepository = accountRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // 1. QUẢN LÝ KHÁCH HÀNG (USERS)
    // ==========================================
    @GetMapping("/admin")
    public String admin(HttpSession session, Model model,
                        @RequestParam(required = false) Long searchId,
                        @RequestParam(required = false) String searchName,
                        @RequestParam(required = false) String searchPhone,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

        Page<UserListDTO> userPage = userService.searchUsers(searchId, searchName, searchPhone, page, size);

        model.addAttribute("userPage", userPage); 
        model.addAttribute("searchId", searchId);
        model.addAttribute("searchName", searchName);
        model.addAttribute("searchPhone", searchPhone);
        
        return "admin";
    }

    @PostMapping("/admin/update-user")
    public String updateUser(@ModelAttribute User updatedUser, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        
        userService.createUpdateRequest(updatedUser);
        
        redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu thay đổi thông tin đã được gửi lên hệ thống và đang chờ xét duyệt!");
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin");
    }

    // ==========================================
    // 2. QUẢN LÝ TÀI KHOẢN (ACCOUNTS)
    // ==========================================
    
    @GetMapping("/admin/account")
    public String adminAccount(HttpSession session, Model model,
                               @RequestParam(required = false) String searchAccNum,
                               @RequestParam(required = false) String searchUsername,
                               @RequestParam(required = false) String searchFullName,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {


        Specification<Account> spec = Specification.where(AccountListService.hasUserRole("user"))
                .and(AccountListService.hasAccountNumber(searchAccNum))
                .and(AccountListService.hasFullName(searchFullName));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Account> accountPage = accountRepository.findAll(spec, pageable);
        
        model.addAttribute("accountPage", accountPage);
        model.addAttribute("searchAccNum", searchAccNum);
        model.addAttribute("searchFullName", searchFullName);
        
        model.addAttribute("username", session.getAttribute("username"));

        return "admin-account";
    }

    // API: LẤY CHI TIẾT USER (DÙNG CHO POP-UP)
    // ==========================================
    @GetMapping("/admin/api/user/{id}")
    @ResponseBody
    public ResponseEntity<UserDetailDTO> getUserDetailsApi(@PathVariable Long id) {
        UserDetailDTO userDetailDTO = userService.getUserDetailById(id);
                if (userDetailDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDetailDTO);
    }

    // API: LẤY DANH SÁCH YÊU CẦU CHỈNH SỬA
    // ==========================================
    @GetMapping("/admin/api/requests")
    @ResponseBody
    public ResponseEntity<Page<UserUpdateRequestDTO>> getRequestsApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").ascending());
        Page<UserUpdateRequest> requests = requestRepository.findAll(pageable);
        
        Page<UserUpdateRequestDTO> dtoPage = requests.map(req -> new UserUpdateRequestDTO(
                req.getId(),
                req.getUser().getUsername(),
                req.getStatus(),
                req.getRequestDate(),
                req.getNewFullName(),
                req.getNewPhoneNumber(),
                req.getNewEmail(),
                req.getNewAddress(),
                req.getNewGender()
        ));
        return ResponseEntity.ok(dtoPage);
    }

    // API: LẤY CHI TIẾT TÀI KHOẢN
    // ===========================
    @GetMapping("/admin/api/account/details/{accountNumber}")
    @ResponseBody
    public ResponseEntity<AccountDetailDTO> getAccountBasicInfoApi(@PathVariable String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        
        User user = account.getUser();
        
        AccountDetailDTO dto = new AccountDetailDTO(
                account.getAccountNumber(),
                account.getDateOpen(),
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getEmail()
        );
        
        return ResponseEntity.ok(dto);
    }

    // API: TẠO TÀI KHOẢN MỚI CHO KHÁCH HÀNG
    // ==========================================
    @PostMapping("/admin/api/user/{userId}/create-account")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createAccountForUser(@PathVariable Long userId) {
        Map<String, String> response = new HashMap<>();
        
        // 1. Kiểm tra xem user có tồn tại không
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            response.put("error", "Không tìm thấy khách hàng trong hệ thống!");
            return ResponseEntity.badRequest().body(response);
        }
        
        User user = userOpt.get();
        boolean isSaved = false;
        Account newAccount = new Account();

        // 2. Vòng lặp Hybrid: Sinh số Luhn -> Lưu DB -> Bắt lỗi trùng lặp
        do {
            String newAccNum = AccountUtils.generateLuhnAccountNumber();
            newAccount.setAccountNumber(newAccNum);
            newAccount.setUser(user);
            newAccount.setBalance(BigDecimal.ZERO); 
            
            try {
                accountRepository.save(newAccount);
                isSaved = true;
                
            } catch (DataIntegrityViolationException e) {
                System.out.println("⚠️ Cảnh báo: Trùng số tài khoản " + newAccNum + ", hệ thống đang tự động tạo lại số mới...");
            }
        } while (!isSaved);

        response.put("success", "Đã mở thành công tài khoản: " + newAccount.getAccountNumber());
        response.put("accountNumber", newAccount.getAccountNumber());
        return ResponseEntity.ok(response);
    }

    // API: TÌM KIẾM NHANH KHÁCH HÀNG CHO FORM TẠI ACCOUNT
    // ==================================================
    @GetMapping("/admin/api/user/search")
    @ResponseBody
    public ResponseEntity<?> searchUserForWizard(@RequestParam String keyword) {
        keyword = keyword.trim();
        Page<UserListDTO> users;
        
        // 1. Kiểm tra nếu từ khóa là số (ID hoặc Số điện thoại)
        if (keyword.matches("\\d+")) {
            try {
                // Thử tìm theo ID khách hàng trước
                Long id = Long.parseLong(keyword);
                users = userService.searchUsers(id, null, null, 0, 1);
                if (users.hasContent()) {
                    return ResponseEntity.ok(users.getContent().get(0));
                }
            } catch (NumberFormatException ignored) {}
            
            // Nếu không tìm thấy ID, tiếp tục tìm theo Số điện thoại
            users = userService.searchUsers(null, null, keyword, 0, 1);
        } else {
            // 2. Nếu từ khóa là chữ, tìm theo Họ tên
            users = userService.searchUsers(null, keyword, null, 0, 1);
        }

        // 3. Trả về kết quả
        if (users.hasContent()) {
            return ResponseEntity.ok(users.getContent().get(0)); // Trả về khách hàng đầu tiên khớp
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Không tìm thấy khách hàng");
            return ResponseEntity.status(404).body(errorResponse);
        }
    }
}