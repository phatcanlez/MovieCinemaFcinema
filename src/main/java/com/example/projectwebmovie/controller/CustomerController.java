package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.MyTicketDTO;
import com.example.projectwebmovie.dto.PagedTickets;
import com.example.projectwebmovie.dto.SeatDTO;
import com.example.projectwebmovie.dto.UpdateAccountDTO;
import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.Booking;
import com.example.projectwebmovie.service.AccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private static final String UPLOAD_DIR = "src/main/resources/static/images/avatar_account/";

    @Autowired
    private AccountService accountService;


    // API endpoint for fetching tickets data
    @GetMapping("/api/my-tickets")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> getMyTicketsApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        logger.info("API request: Fetching tickets for current user - page: {}, size: {}", page, size);

        Account currentAccount = accountService.getCurrentAccount();
        if (currentAccount == null) {
            logger.warn("No authenticated user found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        try {
            PagedTickets pagedTickets = accountService.getMyTicketsPaginated(currentAccount.getAccountId(), page, size);
            logger.info("Found {} tickets for user {} on page {}", pagedTickets.getTickets().size(),
                    currentAccount.getAccountId(), page);

            return ResponseEntity.ok(pagedTickets);
        } catch (Exception e) {
            logger.error("Error loading tickets: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể tải thông tin vé. Vui lòng thử lại sau."));
        }
    }

    // Thymeleaf view endpoint
    @GetMapping("/my-tickets")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public String showMyTicketsPage(Model model) {
        Account currentAccount = accountService.getCurrentAccount();
        model.addAttribute("account", currentAccount);

        logger.info("Rendering my tickets page");
        return "home/userProfile/my-tickets";
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public String showProfilePage(Model model) {
        logger.info("Accessing profile page");
        Account currentAccount = accountService.getCurrentAccount();
        System.out.println("Current account: " + currentAccount);
        if (currentAccount == null) {
            logger.warn("No authenticated user found, redirecting to login");
            return "redirect:/auth/login";
        }
        model.addAttribute("account", currentAccount);
        return "home/userProfile/profile";
    }

    @PostMapping("/profile/update")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public String updateProfile(@Valid @ModelAttribute("updateAccount") UpdateAccountDTO updateAccountDTO,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        logger.info("Received POST request to update profile for accountId: {}", updateAccountDTO.getAccountId());
        // Lấy tài khoản hiện tại để kiểm tra
        Account currentAccount = accountService.getCurrentAccount();
        if (currentAccount == null) {
            logger.warn("No authenticated user found when updating profile");
            return "redirect:/auth/login";
        }

        // Kiểm tra accountId có khớp với người dùng đang đăng nhập không
        if (!currentAccount.getAccountId().equals(updateAccountDTO.getAccountId())) {
            logger.error("Account ID mismatch: {} vs {}", currentAccount.getAccountId(),
                    updateAccountDTO.getAccountId());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi bảo mật: ID tài khoản không khớp");
            return "redirect:/customer/profile";
        }

        // Kiểm tra lỗi validation
        if (result.hasErrors()) {
            logger.warn("Validation errors found: {}", result.getAllErrors());
            // Chuyển lỗi vào model để hiển thị
            model.addAttribute("account", currentAccount);
            model.addAttribute("validationErrors", result.getAllErrors());
            return "home/userProfile/profile";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                updateAccountDTO.setImage(imageFile);
            }
            // Thực hiện cập nhật
            accountService.updateAccount(updateAccountDTO);

            // Kiểm tra xem profile đã hoàn thiện chưa
            Account updatedAccount = accountService.getCurrentAccount();
            if (isProfileComplete(updatedAccount)) {
                accountService.markProfileAsCompleted(updatedAccount.getAccountId());
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công! Hồ sơ của bạn đã hoàn thiện.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
            }

            // PRG Pattern (Post-Redirect-Get) để tránh form resubmission
            return "redirect:/customer/profile";
        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage(), e);
            // Phân tích chi tiết lỗi và đưa ra thông báo phù hợp
            String errorMessage;
            if (e.getMessage().contains("email")) {
                errorMessage = "Email đã được sử dụng bởi tài khoản khác";
            } else if (e.getMessage().contains("phone")) {
                errorMessage = "Số điện thoại đã được sử dụng bởi tài khoản khác";
            } else {
                errorMessage = "Lỗi khi cập nhật thông tin: " + e.getMessage();
            }

            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            redirectAttributes.addAttribute("updateAccount", updateAccountDTO); // Giữ lại dữ liệu đã nhập

            return "redirect:/customer/profile";
        }
    }

    // Phương thức kiểm tra profile có hoàn thiện không
    private boolean isProfileComplete(Account account) {
        return account.getFullName() != null && !account.getFullName().trim().isEmpty() &&
                account.getPhoneNumber() != null && !account.getPhoneNumber().trim().isEmpty() &&
                account.getDateOfBirth() != null &&
                account.getAddress() != null && !account.getAddress().trim().isEmpty();
    }
}