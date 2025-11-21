package com.example.j2ee_project.service.email;

import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.model.dto.EmailHistoryDTO;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EmailServiceInterface {

    /**
     * Gửi email đến một user cụ thể theo ID
     */
    void sendEmailToUserById(Integer userId, String title, String content, Map<String, Object> templateVariables);

    /**
     * Gửi email đến tất cả user có role cụ thể theo roleId
     */
    void sendEmailToUsersByRole(Integer roleId, String title, String content, Map<String, Object> templateVariables);

    /**
     * Gửi email đến danh sách user IDs
     */
    void sendEmailToUserList(List<Integer> userIds, String title, String content, Map<String, Object> templateVariables);

    /**
     * Gửi mã xác nhận cho user mới đăng ký
     */
    void sendVerificationCode(User newUser);

    /**
     * Gửi mật khẩu tạm thời cho user quên mật khẩu
     */
    void sendRandomPassword(User user, String randomPassword);

    /**
     * Gửi thông báo về voucher mới cho user
     */
    void sendVoucherNotification(Integer userId, String voucherCode, String voucherDescription);

    /**
     * Gửi thông báo về voucher mới cho nhiều user
     */
    void sendVoucherNotificationToUsers(List<Integer> userIds, String voucherCode, String voucherDescription);

    /**
     * Random password generator
     */
    String generateRandomPassword();

    /**
     * Lấy lịch sử gửi email với bộ lọc
     */
    Page<EmailHistoryDTO> getEmailHistory(String token, Integer userId, LocalDateTime startDate, LocalDateTime endDate, String type, int offset, int limit);

    /**
     * Lấy chi tiết email theo ID
     */
    EmailHistoryDTO getEmailById(String token, Integer id);
}