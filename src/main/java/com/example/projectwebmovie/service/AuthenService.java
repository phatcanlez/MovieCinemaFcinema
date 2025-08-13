package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.ForgotPasswordDTO;
import com.example.projectwebmovie.dto.RegisterDTO;
import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.Member;
import com.example.projectwebmovie.model.Otp;
import com.example.projectwebmovie.model.Role;
import com.example.projectwebmovie.repository.AccountRepository;
import com.example.projectwebmovie.repository.MemberRepository;
import com.example.projectwebmovie.repository.OtpRepository;
import com.example.projectwebmovie.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AuthenService implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private static final String DEFAULT_AVATAR = "src/main/resources/static/images/avatar_account/img.png";

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IdGenerateService idGenerateService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Checking if admin account exists...");
        if (!accountRepository.existsByUsername("admin")) {
            logger.info("Admin account does not exist, creating admin account...");
            Account adminAccount = new Account();
            adminAccount.setAccountId("admin001");
            adminAccount.setUsername("admin");
            adminAccount.setPassword(passwordEncoder.encode("admin123"));
            adminAccount.setFullName("Administrator");
            adminAccount.setEmail("admin@cinemamax.com");
            adminAccount.setPhoneNumber("0123456789");
            adminAccount.setAddress("CinemaMax HQ");
            adminAccount.setRegisterDate(LocalDate.now());
            adminAccount.setStatus(1);
            adminAccount.setRoleId(1);
            adminAccount.setImage(DEFAULT_AVATAR);

            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseGet(() -> {
                        logger.info("Role ADMIN not found, creating new ADMIN role...");
                        Role newRole = new Role();
                        newRole.setRoleId(1);
                        newRole.setRoleName("ADMIN");
                        return roleRepository.save(newRole);
                    });
            adminAccount.setRole(adminRole);

            accountRepository.save(adminAccount);
            logger.info("Admin account created successfully with username: admin, password: admin123");
        } else {
            logger.info("Admin account already exists, skipping creation.");
        }
    }

    @Transactional
    public void register(RegisterDTO registerDTO) throws Exception {
        logger.info("Starting registration for username: {}", registerDTO.getUsername());
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            logger.error("Password and confirm password do not match for username: {}", registerDTO.getUsername());
            throw new Exception("Mật khẩu và xác nhận mật khẩu không khớp");
        }
        if (accountRepository.existsByUsername(registerDTO.getUsername())) {
            logger.error("Username already exists: {}", registerDTO.getUsername());
            throw new Exception("Tên đăng nhập đã tồn tại");
        }
        if (accountRepository.existsByEmail(registerDTO.getEmail())) {
            logger.error("Email already exists: {}", registerDTO.getEmail());
            throw new Exception("Email đã tồn tại");
        }

        Account account = new Account();
        String accountId = idGenerateService.generateStringId("ACC", Account.class, "accountId");
        account.setUsername(registerDTO.getUsername());
        account.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        account.setEmail(registerDTO.getEmail());
        account.setGender(null);
        account.setAccountId(accountId);
        account.setRegisterDate(LocalDate.now());
        account.setStatus(1);
        account.setFullName(null);
        account.setAddress(null);
        account.setDateOfBirth(null);
        account.setIdentityCard(null);
        account.setPhoneNumber(null);
        account.setImage(DEFAULT_AVATAR);
        account.setProfileCompleted(false);

        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new Exception("Vai trò USER không tồn tại trong hệ thống"));
        account.setRoleId(userRole.getRoleId());
        account.setRole(userRole);

        Member member = new Member();
        String memberId = idGenerateService.generateStringId("MEM", Member.class, "memberId");
        member.setMemberId(memberId);
        member.setScore(0);
        member.setAccountId(account.getAccountId());
        account.setMember(member);

        accountRepository.save(account);
        logger.info("Account saved successfully with accountId: {}", account.getAccountId());

        try {
            Account savedAccount = accountRepository.findById(account.getAccountId())
                    .orElseThrow(() -> new Exception("Không thể tìm thấy tài khoản trong cơ sở dữ liệu sau khi lưu"));
            memberRepository.findById(member.getMemberId())
                    .orElseThrow(() -> new Exception("Không thể tìm thấy thành viên trong cơ sở dữ liệu sau khi lưu"));
            emailService.sendWelcomeEmail(savedAccount.getEmail(), savedAccount.getUsername(),
                    "https://fcinema-7f3f9.ondigitalocean.app/auth/login", "Đăng nhập ngay");
        } catch (Exception e) {
            logger.error("Error verifying saved data or sending email: {}", e.getMessage(), e);
            throw new Exception("Lỗi khi xác minh dữ liệu hoặc gửi email: " + e.getMessage());
        }
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) throws Exception {
        logger.info("Changing password for username: {}", username);
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("Không tìm thấy tài khoản"));
        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            logger.error("Current password incorrect for username: {}", username);
            throw new Exception("Mật khẩu hiện tại không chính xác");
        }
        if (passwordEncoder.matches(newPassword, account.getPassword())) {
            logger.error("New password must be different from current password");
            throw new Exception("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        logger.info("Password changed successfully for username: {}", username);
    }

    public boolean verifyOtp(ForgotPasswordDTO dto) {
        logger.info("Verifying OTP for email: {}, provided OTP: {}", dto.getEmail(), dto.getOtp());
        if (dto.getEmail() == null || dto.getOtp() == null) {
            logger.warn("Email or OTP is null");
            return false;
        }

        // Sử dụng .trim() để loại bỏ khoảng trắng thừa
        String trimmedInputOtp = dto.getOtp().trim();
        logger.info("Trimmed input OTP: {}", trimmedInputOtp);

        Optional<Otp> otpOptional = otpRepository.findByEmailAndCode(dto.getEmail(), trimmedInputOtp);
        if (otpOptional.isEmpty()) {
            logger.warn("No matching OTP found for email: {} with provided OTP: {}", dto.getEmail(), trimmedInputOtp);
            return false;
        }

        Otp otp = otpOptional.get();
        // Sử dụng .trim() cho mã từ database để so sánh chính xác
        String trimmedDbOtp = otp.getCode().trim();
        logger.info("OTP from DB (trimmed): {}, Input OTP (trimmed): {}", trimmedDbOtp, trimmedInputOtp);
        if (!trimmedDbOtp.equals(trimmedInputOtp)) {
            logger.warn("OTP mismatch: DB OTP: {}, Input OTP: {}", trimmedDbOtp, trimmedInputOtp);
            return false;
        }

        if (System.currentTimeMillis() > otp.getExpiryTime()) {
            logger.warn("OTP expired for email: {}, expiry time: {}", dto.getEmail(), otp.getExpiryTime());
            otpRepository.deleteByEmail(dto.getEmail()); // Xóa OTP hết hạn
            return false;
        }

        try {
            otpRepository.delete(otp); // Xóa OTP sau khi xác minh
            logger.info("OTP verified and deleted for email: {}", dto.getEmail());
        } catch (Exception e) {
            logger.error("Failed to delete OTP: {}", e.getMessage());
            return false;
        }
        return true;
    }

    public String resetPassword(ForgotPasswordDTO dto) {
        logger.info("Resetting password for email: {}", dto.getEmail());
        if (dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            logger.warn("New password or confirm password is null");
            return "Vui lòng nhập đầy đủ mật khẩu.";
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            logger.warn("New password and confirm password do not match for email: {}", dto.getEmail());
            return "Mật khẩu mới và xác nhận mật khẩu không khớp.";
        }

        Account account = accountRepository.findByEmail(dto.getEmail());
        if (account == null) {
            logger.warn("Email not found: {}", dto.getEmail());
            return "Email không tồn tại trong hệ thống.";
        }

        try {
            account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            accountRepository.save(account);
            logger.info("Password reset successfully for email: {}", dto.getEmail());
        } catch (Exception e) {
            logger.error("Failed to reset password for email: {} - {}", dto.getEmail(), e.getMessage());
            return "Lỗi khi đặt lại mật khẩu. Vui lòng thử lại.";
        }
        return "Đặt lại mật khẩu thành công.";
    }
}
