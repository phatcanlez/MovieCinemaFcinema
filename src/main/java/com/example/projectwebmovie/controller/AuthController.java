package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.config.CustomUserDetails;
import com.example.projectwebmovie.dto.ChangePasswordDTO;
import com.example.projectwebmovie.dto.ForgotPasswordDTO;
import com.example.projectwebmovie.dto.RegisterDTO;
import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.service.AccountService;
import com.example.projectwebmovie.service.AuthenService;
import com.example.projectwebmovie.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthenService authenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/login")
    public String showLoginPage(Model model,
                                @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "registered", required = false) String registered,
                                @RequestParam(value = "success", required = false) String success,
                                HttpServletRequest request, @AuthenticationPrincipal OAuth2User principal) throws Exception {
        logger.info("Accessing login page");

        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterDTO());
        }

        if (error != null) {
            logger.info("Login failed, retrieving error message");
            AuthenticationException authException = (AuthenticationException) request.getSession()
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

            String errorMessage = "Đăng nhập thất bại. Vui lòng thử lại"; // Default message

            if (authException != null) {
                String originalMessage = authException.getMessage();

                // Chuyển đổi thông báo lỗi từ tiếng Anh sang tiếng Việt
                if (originalMessage != null) {
                    if (originalMessage.contains("Bad credentials") ||
                            originalMessage.contains("bad credentials") ||
                            originalMessage.toLowerCase().contains("invalid username or password")) {
                        errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng";
                    } else if (originalMessage.toLowerCase().contains("locked") ||
                            originalMessage.contains("User account is locked")) {
                        errorMessage = "Tài khoản của bạn đã bị khóa";
                    } else if (originalMessage.toLowerCase().contains("disabled") ||
                            originalMessage.contains("User is disabled")) {
                        errorMessage = "Tài khoản của bạn đã bị vô hiệu hóa";
                    } else if (originalMessage.toLowerCase().contains("expired") ||
                            originalMessage.contains("User account has expired")) {
                        errorMessage = "Tài khoản của bạn đã hết hạn";
                    } else if (originalMessage.toLowerCase().contains("not found") ||
                            originalMessage.contains("User not found")) {
                        errorMessage = "Tài khoản không tồn tại";
                    }
                }
            }

            model.addAttribute("loginError", errorMessage);
            request.getSession().removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }

        // Giữ nguyên toàn bộ phần OAuth2 Google
        if (principal != null) {
            String userName = principal.getAttribute("name");
            String email = principal.getAttribute("email");
            String googleId = principal.getAttribute("sub");
            String picture = principal.getAttribute("picture");
            model.addAttribute("userName", userName);
            model.addAttribute("email", email);
            model.addAttribute("googleId", googleId);
            model.addAttribute("picture", picture);
            logger.info("User is authenticated: Name: {}, Email: {}, Google ID: {}, Picture: {}",
                    userName, email, googleId, picture);

            // Kiểm tra xem người dùng đã đăng ký chưa
            Account existingAccount = accountService.findByEmail(email);

            if (existingAccount != null) {
                logger.info("User already registered with email: {}", email);
                return "redirect:/home"; // Redirect to home page if user is already registered
            } else {
                logger.info("New user registration required for email: {}", email);
            }

            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(googleId);
            registerDTO.setEmail(email);
            registerDTO.setConfirmPassword(googleId);
            registerDTO.setPassword(googleId);
            // Automatically set a default password or handle it as per your requirement
            authenService.register(registerDTO);
            return "redirect:/home?newGoogleUser=true";
        }

        if (logout != null) {
            model.addAttribute("logoutMsg", "Bạn đã đăng xuất thành công.");
        }
        if (registered != null) {
            model.addAttribute("registeredMsg", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        if (success != null) {
            model.addAttribute("successMsg", success);
        }

        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        return "auth/login";
    }
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        logger.info("Accessing register page");
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterDTO());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterDTO registerDTO, BindingResult result,
                           Model model) {
        logger.info("Received registration request for username: {}", registerDTO.getUsername());
        if (result.hasErrors()) {
            logger.warn("Validation errors found: {}", result.getAllErrors());
            return "auth/register";
        }
        try {
            authenService.register(registerDTO);
            logger.info("Registration successful for username: {}", registerDTO.getUsername());
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            logger.error("Error during registration: {}", e.getMessage(), e);
            model.addAttribute("registerError", e.getMessage());
            model.addAttribute("registerForm", registerDTO);
            return "auth/register";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(Model model) {
        logger.info("Accessing change password page");
        Account currentAccount = accountService.getCurrentAccount();
        if (currentAccount == null) {
            logger.warn("No authenticated user found, redirecting to login");
            return "redirect:/auth/login";
        }
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        model.addAttribute("account", currentAccount);
        return "auth/change-password";
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public String processChangePassword(
            @Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO changePasswordDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Processing change password request");

        Account currentAccount = accountService.getCurrentAccount();
        if (currentAccount == null) {
            logger.warn("No authenticated user found during password change, redirecting to login");
            return "redirect:/auth/login";
        }

        // Kiểm tra lỗi validation
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors in change password form");
            model.addAttribute("account", currentAccount);
            return "auth/change-password";
        }

        // Kiểm tra xác nhận mật khẩu
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            logger.warn("New password and confirmation password do not match");
            bindingResult.rejectValue("confirmPassword", "password.mismatch",
                    "Mật khẩu mới và xác nhận mật khẩu không khớp");
            model.addAttribute("account", currentAccount);
            return "auth/change-password";
        }

        try {
            // Thực hiện đổi mật khẩu
            authenService.changePassword(
                    currentAccount.getUsername(),
                    changePasswordDTO.getCurrentPassword(),
                    changePasswordDTO.getNewPassword());

            logger.info("Password changed successfully for user: {}", currentAccount.getUsername());
            redirectAttributes.addAttribute("successMessage", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            return "redirect:/auth/login";

        } catch (Exception e) {
            logger.error("Error changing password: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("changePasswordDTO", changePasswordDTO);
            return "redirect:/auth/change-password";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        logger.info("Accessing forgot password page");
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto, Model model) {
        logger.info("Requesting forgot password for email: {}", dto.getEmail());
        String message;
        try {
            message = emailService.requestForgotPassword(dto.getEmail());
            model.addAttribute("message", message);
        } catch (Exception e) {
            logger.error("Error requesting forgot password: {}", e.getMessage(), e);
            model.addAttribute("message", "Lỗi khi gửi mã xác nhận. Vui lòng thử lại.");
        }
        return "auth/forgot-password";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto, Model model) {
        logger.info("Verifying OTP for email: {}, provided OTP: {}", dto.getEmail(), dto.getOtp());
        if (authenService.verifyOtp(dto)) {
            model.addAttribute("success", "Xác nhận OTP thành công. Vui lòng nhập mật khẩu mới.");
        } else {
            model.addAttribute("message", "OTP không hợp lệ hoặc đã hết hạn. Vui lòng thử lại hoặc yêu cầu mã mới.");
        }
        return "auth/forgot-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto, Model model) {
        logger.info("Resetting password for email: {}", dto.getEmail());
        String message;
        try {
            message = authenService.resetPassword(dto);
            if (message.contains("thành công")) {
                // Mã hóa URL để xử lý ký tự Unicode
                try {
                    String encodedMessage = URLEncoder.encode(message, "UTF-8");
                    return "redirect:/auth/login?success=" + encodedMessage;
                } catch (UnsupportedEncodingException e) {
                    logger.error("Error encoding success message: {}", e.getMessage(), e);
                    return "redirect:/auth/login?success=Đặt lại mật khẩu thành công"; // Fallback
                }
            }
        } catch (Exception e) {
            logger.error("Error resetting password: {}", e.getMessage(), e);
            message = "Lỗi khi đặt lại mật khẩu. Vui lòng thử lại.";
        }
        model.addAttribute("message", message);
        return "auth/forgot-password";
    }
}