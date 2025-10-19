package com.example.j2ee_project.service.email;

import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.request.log.LogRequest;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.service.log.LogServiceInterface;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements EmailServiceInterface {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final LogServiceInterface logService;

    // Role IDs constants
    private static final Integer CUSTOMER_ROLE_ID = 1;
    private static final Integer STAFF_ROLE_ID = 2;
    private static final Integer ADMIN_ROLE_ID = 3;

    // ============================================
    // PHƯƠNG THỨC GỬI EMAIL CƠ BẢN
    // ============================================

    /**
     * Gửi email đến một user cụ thể theo ID
     */
    @Transactional
    public void sendEmailToUserById(Integer userId, String title, String content,
                                    Map<String, Object> templateVariables) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        sendEmail(user, title, content, templateVariables);

        logService.createLog(new LogRequest(
                "emails",
                user.getUserID(),
                "SEND",
                "Sent email: " + title + " to user ID: " + user.getUserID(),
                userId
        ));
    }

    /**
     * Gửi email đến tất cả user có role cụ thể theo roleId
     */
    @Transactional
    public void sendEmailToUsersByRole(Integer roleId, String title, String content,
                                       Map<String, Object> templateVariables) {
        List<User> users = userRepository.findByRoleId(roleId);
        if (users.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng nào với roleId: " + roleId);
        }

        log.info("Sending email to {} users with roleId: {}", users.size(), roleId);

        for (User user : users) {
            try {
                sendEmail(user, title, content, templateVariables);

                logService.createLog(new LogRequest(
                        "emails",
                        user.getUserID(),
                        "SEND",
                        "Sent email: " + title + " to user ID: " + user.getUserID() + " (RoleId: " + roleId + ")",
                        user.getUserID()
                ));
            } catch (Exception e) {
                log.error("Failed to send email to user ID: {}", user.getUserID(), e);
                logService.createLog(new LogRequest(
                        "emails",
                        user.getUserID(),
                        "ERROR",
                        "Failed to send email to user ID: " + user.getUserID() + " - " + e.getMessage(),
                        null
                ));
            }
        }
    }

    /**
     * Gửi email đến danh sách user IDs
     */
    @Transactional
    public void sendEmailToUserList(List<Integer> userIds, String title, String content,
                                    Map<String, Object> templateVariables) {
        log.info("Sending email to {} users", userIds.size());

        for (Integer userId : userIds) {
            try {
                sendEmailToUserById(userId, title, content, templateVariables);
            } catch (ResourceNotFoundException e) {
                log.error("User not found: {}", userId);
                logService.createLog(new LogRequest(
                        "emails",
                        userId,
                        "ERROR",
                        "Failed to send email to user ID: " + userId + " - User not found",
                        null
                ));
            }
        }
    }

    // ============================================
    // PHƯƠNG THỨC GỬI EMAIL CHO TỪNG CHỨC NĂNG CỤ THỂ
    // ============================================

    /**
     * Gửi mã xác nhận cho user mới đăng ký
     */
    @Transactional
    public void sendVerificationCode(User newUser) {
        if (newUser.getEmail() == null || newUser.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required for verification");
        }
        String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        newUser.setVerifyCode(verificationCode);
        userRepository.save(newUser);

        Map<String, Object> variables = new HashMap<>();
        variables.put("Mã xác nhận", verificationCode);

        sendEmail(newUser, "Xác nhận tài khoản",
                "Mã xác nhận của bạn là: " + verificationCode, variables);

        logService.createLog(new LogRequest(
                "emails",
                newUser.getUserID(),
                "SEND",
                "Sent verification email to user ID: " + newUser.getUserID(),
                newUser.getUserID()
        ));
    }

    /**
     * Gửi mật khẩu tạm thời cho user quên mật khẩu
     */
    @Transactional
    public void sendRandomPassword(User user, String randomPassword) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required for password reset");
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("Mật khẩu tạm thời", randomPassword);

        sendEmail(user, "Mật khẩu mới",
                "Mật khẩu tạm thời của bạn là: " + randomPassword + ". Vui lòng đổi mật khẩu ngay sau khi đăng nhập!",
                variables);

        logService.createLog(new LogRequest(
                "emails",
                user.getUserID(),
                "SEND",
                "Sent password reset email to user ID: " + user.getUserID(),
                user.getUserID()
        ));
    }

    /**
     * Gửi thông báo về voucher mới cho user
     */
    @Transactional
    public void sendVoucherNotification(Integer userId, String voucherCode, String voucherDescription) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("Mã voucher", voucherCode);
        variables.put("Mô tả voucher", voucherDescription);

        sendEmailToUserById(userId, "🎉 Bạn có voucher mới!",
                "Chúc mừng! Bạn vừa nhận được voucher: " + voucherDescription + " với mã: " + voucherCode,
                variables);
    }

    /**
     * Gửi thông báo về voucher mới cho nhiều user
     */
    @Transactional
    public void sendVoucherNotificationToUsers(List<Integer> userIds, String voucherCode, String voucherDescription) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("Mã voucher", voucherCode);
        variables.put("Mô tả voucher", voucherDescription);

        sendEmailToUserList(userIds, "🎉 Bạn có voucher mới!",
                "Chúc mừng! Bạn vừa nhận được voucher: " + voucherDescription + " với mã: " + voucherCode,
                variables);
    }

    // ============================================
    // PHƯƠNG THỨC HỖ TRỢ
    // ============================================

    /**
     * Method chính để gửi email với retry mechanism
     */
    @Retryable(
            value = {MessagingException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private void sendEmail(User user, String title, String content,
                           Map<String, Object> templateVariables) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Người dùng không có email: " + user.getUserID());
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject(title);

            String emailContent = buildEmailContent(user, content, templateVariables);
            helper.setText(emailContent, false); // false = plain text

            mailSender.send(message);

            log.info("Email sent successfully to: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", user.getEmail(), e);
            logService.createLog(new LogRequest(
                    "emails",
                    user.getUserID(),
                    "ERROR",
                    "Failed to send email: " + title + " to " + user.getEmail() + " - " + e.getMessage(),
                    user.getUserID()
            ));
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Xây dựng nội dung email plain text
     */
    private String buildEmailContent(User user, String content, Map<String, Object> templateVariables) {
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("Xin chào ").append(user.getFullName()).append("!\n\n");
        emailContent.append(content).append("\n\n");

        // Thêm các biến template vào nội dung
        if (templateVariables != null && !templateVariables.isEmpty()) {
            templateVariables.forEach((key, value) -> {
                emailContent.append(key).append(": ").append(value).append("\n");
            });
        }

        emailContent.append("\nTrân trọng,\nĐội ngũ hỗ trợ");

        return emailContent.toString();
    }

    /**
     * Hàm tạo random password
     */
    // Phương thức tiện ích để tạo mật khẩu ngẫu nhiên
    public String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(10); // Độ dài 10 ký tự
        for (int i = 0; i < 10; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}