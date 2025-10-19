package com.example.j2ee_project.controller;

import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.request.email.EmailRequest;
import com.example.j2ee_project.model.request.email.EmailVerificationRequest;
import com.example.j2ee_project.model.request.email.PasswordResetRequest;
import com.example.j2ee_project.model.response.ResponseData;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.email.EmailServiceInterface;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@Tag(name = "Email Management", description = "APIs for sending and managing email notifications")
@RequiredArgsConstructor
public class EmailController {

    private final EmailServiceInterface emailService;
    private final ResponseHandler responseHandler;

    /**
     * Gửi email linh hoạt dựa trên loại gửi (to user, to list, to role)
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ResponseData> sendEmail(@Valid @RequestBody EmailRequest request, BindingResult bindingResult) {
        // Xử lý lỗi validation
        if (bindingResult.hasErrors()) {
            return responseHandler.handleValidationErrors(bindingResult);
        }

        try {
            switch (request.getSendType()) {
                case USER:
                    emailService.sendEmailToUserById(
                            request.getUserId(),
                            request.getTitle(),
                            request.getContent(),
                            request.getTemplateVariables()
                    );
                    return responseHandler.responseSuccess("Email đã được gửi thành công đến user", null);

                case LIST:
                    emailService.sendEmailToUserList(
                            request.getUserIds(),
                            request.getTitle(),
                            request.getContent(),
                            request.getTemplateVariables()
                    );
                    return responseHandler.responseSuccess("Email đã được gửi đến danh sách user", null);

                case ROLE:
                    emailService.sendEmailToUsersByRole(
                            request.getRoleId(),
                            request.getTitle(),
                            request.getContent(),
                            request.getTemplateVariables()
                    );
                    return responseHandler.responseSuccess("Email đã được gửi đến tất cả user có roleId: " + request.getRoleId(), null);

                default:
                    return responseHandler.handleBadRequest("Invalid sendType: " + request.getSendType());
            }
        } catch (IllegalArgumentException e) {
            return responseHandler.handleBadRequest(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return responseHandler.handleNotFound(e.getMessage());
        } catch (Exception e) {
            return responseHandler.handleServerError("Lỗi khi gửi email: " + e.getMessage());
        }
    }

    /**
     * Gửi mã xác nhận cho user mới (tích hợp với registration flow)
     */
    @PostMapping("/send-verification")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ResponseData> sendVerificationCode(@Valid @RequestBody EmailVerificationRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return responseHandler.handleValidationErrors(bindingResult);
        }

        try {
            User user = new User();
            user.setUserID(request.getUserId());
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName()); // Nếu cần thêm thông tin
            emailService.sendVerificationCode(user);
            return responseHandler.responseSuccess("Email xác nhận đã được gửi", null);
        } catch (IllegalArgumentException e) {
            return responseHandler.handleBadRequest(e.getMessage());
        } catch (Exception e) {
            return responseHandler.handleServerError("Lỗi khi gửi email xác nhận: " + e.getMessage());
        }
    }

    /**
     * Gửi mật khẩu tạm thời (tích hợp với password reset flow)
     */
    @PostMapping("/send-password-reset")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ResponseData> sendRandomPassword(@Valid @RequestBody PasswordResetRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return responseHandler.handleValidationErrors(bindingResult);
        }

        try {
            // Tạo mật khẩu ngẫu nhiên
            String randomPassword = emailService.generateRandomPassword();

            User user = new User();
            user.setUserID(request.getUserId());
            user.setEmail(request.getEmail());
            emailService.sendRandomPassword(user, randomPassword);
            return responseHandler.responseSuccess("Email đặt lại mật khẩu đã được gửi", null);
        } catch (IllegalArgumentException e) {
            return responseHandler.handleBadRequest(e.getMessage());
        } catch (Exception e) {
            return responseHandler.handleServerError("Lỗi khi gửi email đặt lại mật khẩu: " + e.getMessage());
        }
    }

    /**
     * Gửi email chào mừng (tích hợp với registration flow)
     */
    @PostMapping("/send-welcome")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ResponseData> sendWelcomeEmail(@Valid @RequestBody EmailVerificationRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return responseHandler.handleValidationErrors(bindingResult);
        }

        try {
            User user = new User();
            user.setUserID(request.getUserId());
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName()); // Nếu cần thêm thông tin
            Map<String, Object> variables = new HashMap<>();
            variables.put("welcomeMessage", "Chào mừng bạn đến với hệ thống của chúng tôi!");
            emailService.sendEmailToUserById(
                    user.getUserID(),
                    "Chào mừng bạn!",
                    "Cảm ơn bạn đã đăng ký tài khoản. Chúng tôi rất vui khi bạn tham gia cộng đồng của chúng tôi!",
                    variables
            );
            return responseHandler.responseSuccess("Email chào mừng đã được gửi", null);
        } catch (IllegalArgumentException e) {
            return responseHandler.handleBadRequest(e.getMessage());
        } catch (Exception e) {
            return responseHandler.handleServerError("Lỗi khi gửi email chào mừng: " + e.getMessage());
        }
    }
}