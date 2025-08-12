package com.example.projectwebmovie.service;

import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.Booking;
import com.example.projectwebmovie.model.Otp;
import com.example.projectwebmovie.model.Payment;
import com.example.projectwebmovie.repository.AccountRepository;
import com.example.projectwebmovie.repository.OtpRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Phương thức nội bộ để gửi email với nội dung HTML tùy chỉnh
     * @param to Email người nhận
     * @param subject Tiêu đề email
     * @param htmlContent Nội dung HTML
     * @throws MessagingException Nếu gửi email thất bại
     */
    private void sendEmailInternal(String to, String subject, String htmlContent) throws MessagingException {
        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            throw new IllegalStateException("Email sender is not configured in application.properties");
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        mimeMessageHelper.setFrom(fromEmail);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(htmlContent, true); // true = gửi dưới dạng HTML
        javaMailSender.send(mimeMessage);
        logger.info("Email sent successfully from {} to: {}", fromEmail, to);
    }

    /**
     * Gửi email chào mừng với nội dung HTML tùy chỉnh
     * @param to Email người nhận
     * @param name Tên người nhận (dùng để chào)
     * @param link Liên kết đăng nhập
     * @param buttonText Văn bản nút
     */
    public void sendWelcomeEmail(String to, String name, String link, String buttonText) {
        try {
            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang=\"vi\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<title>Chào mừng đến với CinemaMax</title>" +
                    "<style>" +
                    "body { font-family: 'Arial', sans-serif; background: linear-gradient(135deg, #0a0a0a, #1a1a1a); color: #ffffff; margin: 0; padding: 0; line-height: 1.6; }" +
                    ".container { max-width: 650px; margin: 40px auto; background: linear-gradient(135deg, #2d0000, #640000); border-radius: 15px; overflow: hidden; box-shadow: 0 0 30px rgba(100, 0, 0, 0.7); position: relative; }" +
                    ".header::before { content: ''; position: absolute; top: -50px; left: -50px; width: 200px; height: 200px; background: radial-gradient(circle, rgba(255, 50, 50, 0.3) 0%, transparent 70%); animation: glowPulse 8s ease-in-out infinite; }" +
                    ".header { background: linear-gradient(135deg, #640000, #3c0000); padding: 30px; text-align: center; position: relative; border-bottom: 3px solid rgba(255, 255, 255, 0.1); }" +
                    ".header h1 { margin: 0; font-size: 32px; color: #ffffff; text-shadow: 0 0 15px rgba(255, 50, 50, 0.5); }" +
                    ".content { padding: 40px; text-align: center; }" +
                    ".content p { font-size: 16px; color: #e0e0e0; margin-bottom: 20px; }" +
                    ".content .highlight { color: #ff6666; font-weight: bold; }" +
                    ".button { display: inline-block; padding: 15px 30px; margin-top: 20px; background: linear-gradient(135deg, #ff3333, #cc0000); color: #ffffff; text-decoration: none; border-radius: 8px; font-size: 18px; font-weight: 600; transition: all 0.4s ease; box-shadow: 0 5px 15px rgba(255, 50, 50, 0.4); text-transform: uppercase; }" +
                    ".button:hover { background: linear-gradient(135deg, #ff6666, #e60000); transform: translateY(-3px); box-shadow: 0 8px 20px rgba(255, 50, 50, 0.6); }" +
                    ".footer { text-align: center; padding: 20px; font-size: 12px; color: #aaaaaa; background: #2d0000; border-top: 1px solid rgba(100, 0, 0, 0.3); }" +
                    ".footer a { color: #ff6666; text-decoration: none; transition: color 0.3s ease; }" +
                    ".footer a:hover { color: #ff9999; }" +
                    "@keyframes glowPulse { 0% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.2); opacity: 0.8; } 100% { transform: scale(1); opacity: 0.5; } }" +
                    "@media (max-width: 600px) { .container { margin: 20px; width: calc(100% - 40px); } .header h1 { font-size: 24px; } .content { padding: 20px; } .content p { font-size: 14px; } .button { padding: 12px 25px; font-size: 16px; } }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class=\"container\">" +
                    "<div class=\"header\">" +
                    "<h1>Chào mừng bạn đến với <span class=\"highlight\">CinemaMax</span>!</h1>" +
                    "</div>" +
                    "<div class=\"content\">" +
                    "<p>Xin chào " + name + ",</p>" +
                    "<p>Cảm ơn bạn đã đăng ký tài khoản tại CinemaMax. Vui lòng đăng nhập để cập nhật thông tin cá nhân và khám phá thế giới điện ảnh đỉnh cao!</p>" +
                    "<a href=\"" + link + "\" class=\"button\">" + buttonText + "</a>" +
                    "</div>" +
                    "<div class=\"footer\">" +
                    "<p>© 2025 CinemaMax. <a href=\"mailto:support@cinemamax.com\">Liên hệ hỗ trợ</a> | <a href=\"#\">Điều khoản sử dụng</a></p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            sendEmailInternal(to, "Chào mừng bạn đến với CinemaMax!", htmlContent);
        } catch (MessagingException e) {
            logger.error("Failed to send welcome email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi gửi email chào mừng: " + e.getMessage(), e);
        }
    }

    /**
     * Gửi email với nội dung HTML tùy chỉnh (không dùng template)
     * @param to Email người nhận
     * @param subject Tiêu đề email
     * @param htmlContent Nội dung HTML
     */
    public void sendCustomEmail(String to, String subject, String htmlContent) {
        try {
            sendEmailInternal(to, subject, htmlContent);
        } catch (MessagingException e) {
            logger.error("Failed to send custom email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi gửi email tùy chỉnh: " + e.getMessage(), e);
        }
    }

    /**
     * Gửi email thông báo lỗi runtime
     * @param emailDetail Nội dung lỗi để gửi
     */
    public void sendEmailErrorRuntime(String emailDetail) {
        try {
            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang=\"vi\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<title>Error Runtime</title>" +
                    "<style>" +
                    "body { font-family: 'Arial', sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                    ".container { max-width: 600px; margin: 20px auto; background-color: #fff; padding: 20px; border: 1px solid #ddd; }" +
                    ".error { color: #d9534f; font-size: 18px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class=\"container\">" +
                    "<h2 class=\"error\">Lỗi Runtime</h2>" +
                    "<p>Thời gian: " + java.time.LocalDateTime.now() + "</p>" +
                    "<p>Chi tiết lỗi: " + emailDetail + "</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            sendEmailInternal("phatcanlez@gmail.com", "Error Runtime", htmlContent);
        } catch (MessagingException e) {
            logger.error("Failed to send error email: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi gửi email lỗi: " + e.getMessage(), e);
        }
    }

    /**
     * Gửi email kiểm tra
     */
    public void testMail() {
        try {
            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang=\"vi\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<title>Test Email</title>" +
                    "<style>" +
                    "body { font-family: 'Arial', sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                    ".container { max-width: 600px; margin: 20px auto; background-color: #fff; padding: 20px; border: 1px solid #ddd; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class=\"container\">" +
                    "<h2>Test Email</h2>" +
                    "<p>Thời gian: " + java.time.LocalDateTime.now() + "</p>" +
                    "<p>Nội dung: This is a test email</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            sendEmailInternal("phatcanlez@gmail.com", "Test Email", htmlContent);
        } catch (MessagingException e) {
            logger.error("Failed to send test email: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi gửi email kiểm tra: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String requestForgotPassword(String email) {
        logger.info("Requesting forgot password for email: {}", email);
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            logger.warn("Email not found in database: {}", email);
            return "Email không tồn tại trong hệ thống.";
        }

        // Delete old OTPs
        logger.info("Deleting old OTPs for email: {}", email);
        try {
            otpRepository.deleteByEmail(email);
            logger.info("Old OTPs deleted successfully for email: {}", email);
        } catch (Exception e) {
            logger.error("Failed to delete old OTPs for email: {} - {}", email, e.getMessage());
            return "Lỗi khi xóa mã xác nhận cũ. Vui lòng thử lại.";
        }

        // Generate and save new OTP
        String otpCode = generateOtp(6);
        Long expiryTime = LocalDateTime.now().plusMinutes(10).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setCode(otpCode);
        otp.setExpiryTime(expiryTime);
        try {
            otpRepository.save(otp);
            logger.info("New OTP generated and saved for email: {} with code: {}", email, otpCode);
        } catch (Exception e) {
            logger.error("Failed to save new OTP for email: {} - {}", email, e.getMessage());
            return "Lỗi khi tạo mã xác nhận mới. Vui lòng thử lại.";
        }

        // Send styled OTP email
        try {
            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang=\"vi\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<title>Mã xác nhận đặt lại mật khẩu</title>" +
                    "<style>" +
                    "body { font-family: 'Arial', sans-serif; background: linear-gradient(135deg, #0a0a0a, #1a1a1a); color: #ffffff; margin: 0; padding: 0; line-height: 1.6; }" +
                    ".container { max-width: 650px; margin: 40px auto; background: linear-gradient(135deg, #2d0000, #640000); border-radius: 15px; overflow: hidden; box-shadow: 0 0 30px rgba(100, 0, 0, 0.7); position: relative; }" +
                    ".header::before { content: ''; position: absolute; top: -50px; left: -50px; width: 200px; height: 200px; background: radial-gradient(circle, rgba(255, 50, 50, 0.3) 0%, transparent 70%); animation: glowPulse 8s ease-in-out infinite; }" +
                    ".header { background: linear-gradient(135deg, #640000, #3c0000); padding: 30px; text-align: center; position: relative; border-bottom: 3px solid rgba(255, 255, 255, 0.1); }" +
                    ".header h1 { margin: 0; font-size: 32px; color: #ffffff; text-shadow: 0 0 15px rgba(255, 50, 50, 0.5); }" +
                    ".content { padding: 40px; text-align: center; }" +
                    ".content p { font-size: 16px; color: #e0e0e0; margin-bottom: 20px; }" +
                    ".content .highlight { color: #ff6666; font-weight: bold; }" +
                    ".otp-code { font-size: 24px; color: #ff6666; font-weight: bold; padding: 15px; background: rgba(255, 255, 255, 0.1); border-radius: 8px; display: inline-block; margin: 20px 0; }" +
                    ".footer { text-align: center; padding: 20px; font-size: 12px; color: #aaaaaa; background: #2d0000; border-top: 1px solid rgba(100, 0, 0, 0.3); }" +
                    ".footer a { color: #ff6666; text-decoration: none; transition: color 0.3s ease; }" +
                    ".footer a:hover { color: #ff9999; }" +
                    "@keyframes glowPulse { 0% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.2); opacity: 0.8; } 100% { transform: scale(1); opacity: 0.5; } }" +
                    "@media (max-width: 600px) { .container { margin: 20px; width: calc(100% - 40px); } .header h1 { font-size: 24px; } .content { padding: 20px; } .content p { font-size: 14px; } .otp-code { font-size: 20px; padding: 10px; } }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class=\"container\">" +
                    "<div class=\"header\">" +
                    "<h1>Đặt lại mật khẩu <span class=\"highlight\">CinemaMax</span></h1>" +
                    "</div>" +
                    "<div class=\"content\">" +
                    "<p>Xin chào,</p>" +
                    "<p>Bạn đã yêu cầu đặt lại mật khẩu. Mã xác nhận của bạn là:</p>" +
                    "<div class=\"otp-code\">" + otpCode + "</div>" +
                    "<p>Mã này có hiệu lực trong <span class=\"highlight\">10 phút</span>. Vui lòng sử dụng để đặt lại mật khẩu.</p>" +
                    "</div>" +
                    "<div class=\"footer\">" +
                    "<p>© 2025 CinemaMax. <a href=\"mailto:support@cinemamax.com\">Liên hệ hỗ trợ</a> | <a href=\"#\">Điều khoản sử dụng</a></p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            this.sendCustomEmail(email, "Mã xác nhận đặt lại mật khẩu", htmlContent);
            logger.info("OTP email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", email, e.getMessage());
            return "Lỗi khi gửi mã xác nhận qua email. Vui lòng kiểm tra kết nối hoặc thử lại.";
        }

        return "Mã xác nhận đã được gửi tới email của bạn.";
    }

    private String generateOtp(int length) {
        String numbers = "0123456789";
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            otp.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        return otp.toString();
    }

    // Method gốc (synchronous)
    public void sendPaymentConfirmationEmail(String customerEmail, Booking booking) {
        try {
//            String customerName = booking.getAccount().getFullName();
            Payment payment = booking.getPayments().get(0);

            // Tạo thông tin ghế
            String seatInfo = booking.getBookingSeats().stream()
                    .map(seat -> seat.getScheduleSeat().getSeatRow() + seat.getScheduleSeat().getSeatColumn())
                    .reduce((seat1, seat2) -> seat1 + ", " + seat2)
                    .orElse("N/A");

            // Tạo thông tin combo
            String comboInfo = booking.getBookingCombos().stream()
                    .map(combo -> combo.getCombo().getComboName() + " x" + combo.getQuantity())
                    .reduce((combo1, combo2) -> combo1 + ", " + combo2)
                    .orElse("");

            // Đọc template HTML từ file
            String htmlContent = loadPaymentConfirmationTemplate( booking, payment, seatInfo, comboInfo);

            sendEmailInternal(customerEmail, "Xác nhận thanh toán thành công - CinemaMax", htmlContent);
            logger.info("Payment confirmation email sent successfully to: {}", customerEmail);
        } catch (Exception e) {
            logger.error("Failed to send payment confirmation email to {}: {}", customerEmail, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi gửi email xác nhận thanh toán: " + e.getMessage(), e);
        }
    }

    // Method async cho việc gửi email
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendPaymentConfirmationEmailAsync(String customerEmail, Booking booking) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Starting async email sending to: {} on thread: {}", customerEmail, Thread.currentThread().getName());
                sendPaymentConfirmationEmail(customerEmail, booking);
                logger.info("Async email sent successfully to: {} on thread: {}", customerEmail, Thread.currentThread().getName());
            } catch (Exception e) {
                logger.error("Failed to send async email to {}: {} on thread: {}", customerEmail, e.getMessage(), Thread.currentThread().getName());
                // Không throw exception để không ảnh hưởng đến main thread
            }
        });
    }

    // Method async với callback để xử lý kết quả
    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendPaymentConfirmationEmailAsyncWithResult(String customerEmail, Booking booking) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting async email sending to: {} on thread: {}", customerEmail, Thread.currentThread().getName());
                sendPaymentConfirmationEmail(customerEmail, booking);
                logger.info("Async email sent successfully to: {} on thread: {}", customerEmail, Thread.currentThread().getName());
                return true;
            } catch (Exception e) {
                logger.error("Failed to send async email to {}: {} on thread: {}", customerEmail, e.getMessage(), Thread.currentThread().getName());
                return false;
            }
        });
    }

    /**
     * Load payment confirmation email template
     */
    private String loadPaymentConfirmationTemplate(Booking booking, Payment payment,
                                                   String seatInfo, String comboInfo) {
        try {
            // Đọc file HTML từ resources
            String templatePath = "/templates/mail/payment-confirmation.html";
            InputStream inputStream = getClass().getResourceAsStream(templatePath);

            if (inputStream == null) {
                throw new RuntimeException("Template file not found: " + templatePath);
            }

            String template = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            logger.info("Payment confirmation email data for booking ID: {}", booking.getBookingId());
//            logger.info("customerName: {}", customerName);
            logger.info("bookingId: {}", booking.getBookingId());
            logger.info("paymentId: {}", payment.getPaymentId());
            logger.info("paymentMethod: {}", payment.getPaymentMethod().name());
            logger.info("paymentDate: {}", payment.getPaymentDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            logger.info("paymentStatus: {}", payment.getStatus());
            logger.info("movieName: {}", booking.getMovie().getMovieNameVn());
            logger.info("cinemaRoom: {}", booking.getSchedule().getMovieSchedules().get(0).getCinemaRoom().getCinemaRoomName());
            logger.info("showTime: {}", booking.getSchedule().getScheduleTime());
            logger.info("showDate: {}", booking.getShowDates().getShowDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            logger.info("seatInfo: {}", seatInfo);
            logger.info("ticketCount: {}", booking.getBookingSeats().size());
            logger.info("comboInfo: {}", comboInfo.isEmpty() ? "Không có" : comboInfo);
            logger.info("usedPoints: {}", booking.getUsedPoints() != null ? booking.getUsedPoints().toString() : "0");
            logger.info("totalAmount: {}", String.format("%,.0f", booking.getTotalPrice()));
            logger.info("qrCode: {}", booking.getBookingId());
            // Replace placeholders với dữ liệu thực
            return template
//                    .replace("{{customerName}}", customerName)
                    .replace("{{bookingId}}", booking.getBookingId())
                    .replace("{{paymentId}}", payment.getPaymentId())
                    .replace("{{paymentMethod}}", payment.getPaymentMethod().name())
                    .replace("{{paymentDate}}", payment.getPaymentDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .replace("{{paymentStatus}}", payment.getStatus())
                    .replace("{{movieName}}", booking.getMovie().getMovieNameVn())
                    .replace("{{cinemaRoom}}", booking.getSchedule().getMovieSchedules().get(0).getCinemaRoom().getCinemaRoomName())
                    .replace("{{showTime}}", booking.getSchedule().getScheduleTime())
                    .replace("{{showDate}}", booking.getShowDates().getShowDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .replace("{{seatInfo}}", seatInfo)
                    .replace("{{ticketCount}}", String.valueOf(booking.getBookingSeats().size()))
                    .replace("{{comboInfo}}", comboInfo.isEmpty() ? "Không có" : comboInfo)
                    .replace("{{usedPoints}}", booking.getUsedPoints() != null ? booking.getUsedPoints().toString() : "0")
                    .replace("{{totalAmount}}", String.format("%,.0f", booking.getTotalPrice()))
                    .replace("{{qrCode}}", booking.getBookingId());
        } catch (Exception e) {
            logger.error("Error loading payment confirmation template: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi tải template email: " + e.getMessage(), e);
        }
    }

}