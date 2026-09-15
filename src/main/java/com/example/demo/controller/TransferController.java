package com.example.demo.controller;

import com.example.demo.dto.DashboardAccountDTO;
import com.example.demo.dto.TransferRequestDTO;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientBalanceException;
import com.example.demo.exception.InvalidTransferException;
import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.model.enums.AccountType;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AccountListService;
import com.example.demo.service.TransferService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/dashboard/transfer")
public class TransferController {

    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // Ghi nhận các idempotencyKey đã xử lý (theo userId) để chặn xử lý trùng khi client
    // (do double-click, mạng chập chờn dẫn tới người dùng bấm lại, v.v.) gửi lên 2 request
    // mang cùng 1 token cho cùng 1 lần "Xác nhận". Khác với RateLimitInterceptor (chặn theo
    // thời gian, không đáng tin cậy để chống double-submit), cơ chế này chặn theo đúng hành
    // động cụ thể của người dùng, không phụ thuộc khoảng cách thời gian giữa 2 request.
    private final Cache<String, Boolean> processedIdempotencyKeys = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public TransferController(TransferService transferService, AccountRepository accountRepository, UserRepository userRepository) {
        this.transferService = transferService;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    // CHUẨN BỊ FORM (GET)
    @GetMapping
    public String showTransferForm(HttpSession session, Model model) {
        Long currentUserId = (Long) session.getAttribute("userId");
        User currentUser = userRepository.findById(currentUserId).orElseThrow();

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("user", currentUser);
        model.addAttribute("sourceAccounts", loadSourceAccounts(currentUserId));
        model.addAttribute("allMyAccounts", loadAllMyAccounts(currentUserId));
        model.addAttribute("transferRequest", new TransferRequestDTO());

        return "dashboard-transfer";
    }

    // XỬ LÝ CHUYỂN TIỀN (POST)
    @PostMapping
    public String processTransfer(@Valid @ModelAttribute("transferRequest") TransferRequestDTO transferRequest,
                                   BindingResult bindingResult,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        Long currentUserId = (Long) session.getAttribute("userId");

        if (bindingResult.hasErrors()) {
            User currentUser = userRepository.findById(currentUserId).orElseThrow();
            model.addAttribute("username", session.getAttribute("username"));
            model.addAttribute("user", currentUser);
            model.addAttribute("sourceAccounts", loadSourceAccounts(currentUserId));
            model.addAttribute("allMyAccounts", loadAllMyAccounts(currentUserId));

            // Lấy message lỗi để hiển thị cho người dùng (trước đây bị bỏ sót -> không có thông báo nào hiện ra)
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(err -> {
                        // Lỗi do @Positive/@Digits/@NotNull... trả về message tự định nghĩa trong DTO
                        if (!"typeMismatch".equals(err.getCode())) {
                            return err.getDefaultMessage();
                        }
                        // Lỗi do bind kiểu dữ liệu thất bại (VD: nhập "abc"/số âm sai định dạng vào field BigDecimal)
                        // -> Spring tự sinh message kỹ thuật, không thân thiện, nên thay bằng message rõ ràng theo field
                        if ("amount".equals(err.getField())) {
                            return "Số tiền không hợp lệ, vui lòng nhập đúng định dạng số!";
                        }
                        return "Dữ liệu nhập vào không hợp lệ, vui lòng kiểm tra lại!";
                    })
                    .filter(msg -> msg != null && !msg.isBlank())
                    .findFirst()
                    .orElse("Dữ liệu nhập vào không hợp lệ, vui lòng kiểm tra lại!");

            logger.warn("Transfer rejected (binding/validation) - userId={}, errors={}", currentUserId, errorMessage);
            model.addAttribute("txErrorMessage", errorMessage);
            return "dashboard-transfer";
        }

        // CHẶN GỬI TRÙNG (IDEMPOTENCY CHECK)
        // putIfAbsent là thao tác atomic: nếu 2 request cùng token tới gần như đồng thời,
        // chỉ 1 request "thắng" (nhận về null) và được xử lý; request còn lại bị chặn ở đây,
        // không tạo giao dịch mới, tránh trừ tiền 2 lần dù có vượt qua được RateLimitInterceptor.
        String idempotencyKey = transferRequest.getIdempotencyKey();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String cacheKey = currentUserId + ":" + idempotencyKey;
            boolean isDuplicate = processedIdempotencyKeys.asMap().putIfAbsent(cacheKey, Boolean.TRUE) != null;
            if (isDuplicate) {
                logger.warn("Duplicate transfer submission ignored - userId={}, idempotencyKey={}", currentUserId, idempotencyKey);
                redirectAttributes.addFlashAttribute("txErrorMessage",
                        "Yêu cầu chuyển khoản này đã được ghi nhận trước đó. Vui lòng kiểm tra lịch sử giao dịch trước khi thử lại.");
                return "redirect:/dashboard/transfer";
            }
        }

        try {
            Transaction result = transferService.transferMoney(currentUserId, transferRequest);
            redirectAttributes.addFlashAttribute("txSuccessMessage", "Chuyển tiền thành công! Mã giao dịch: " + result.getTransactionId());
            return "redirect:/dashboard/transfer";

        } catch (AccountNotFoundException | InsufficientBalanceException | InvalidTransferException ex) {
            logger.warn("Transfer rejected - userId={}, from={}, to={}, amount={}, reason={}", currentUserId, transferRequest.getFromAccountNumber(), transferRequest.getToAccountNumber(), transferRequest.getAmount(), ex.getMessage());
            redirectAttributes.addFlashAttribute("txErrorMessage", ex.getMessage());
            return "redirect:/dashboard/transfer";

        } catch (ObjectOptimisticLockingFailureException ex) {
            logger.warn("Transfer conflict - userId={}", currentUserId, ex);
            redirectAttributes.addFlashAttribute("txErrorMessage", "Tài khoản của bạn đang xử lý một giao dịch khác. Vui lòng đợi trong giây lát và thử lại!");
            return "redirect:/dashboard/transfer";
        
        } catch (Exception ex) {
            logger.error("Transfer failed with unexpected error", ex);
            redirectAttributes.addFlashAttribute("txErrorMessage", "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau!");
            return "redirect:/dashboard/transfer";
        }
    }

    private List<DashboardAccountDTO> loadSourceAccounts(Long userId) {
        List<Account> accounts = accountRepository.findAll(AccountListService.hasUserId(userId));
        return accounts.stream()
                .filter(acc -> acc.getAccountType() == AccountType.PAYMENT)
                .map(acc -> new DashboardAccountDTO(acc.getAccountNumber(), acc.getAccountType(), acc.getTransactionLimit(), acc.getBalance(), acc.getDateOpen(), acc.getInterestRate(), acc.getTermMonths()))
                .toList();
    }

    private List<DashboardAccountDTO> loadAllMyAccounts(Long userId) {
        List<Account> accounts = accountRepository.findAll(AccountListService.hasUserId(userId));
        return accounts.stream()
                .map(acc -> new DashboardAccountDTO(acc.getAccountNumber(), acc.getAccountType(), acc.getTransactionLimit(), acc.getBalance(), acc.getDateOpen(), acc.getInterestRate(), acc.getTermMonths()))
                .toList();
    }
}