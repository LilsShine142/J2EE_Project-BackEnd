package com.example.j2ee_project.controller;

import com.example.j2ee_project.exception.ForbiddenException;
import com.example.j2ee_project.model.dto.NotificationDTO;
import com.example.j2ee_project.model.request.notification.BroadcastNotificationRequest;
import com.example.j2ee_project.model.request.notification.NotificationRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.notification.NotificationServiceInterface;
import com.example.j2ee_project.utils._enum.EPermission;
import com.example.j2ee_project.utils.role_permission.RolePermissionUtils;
import com.example.j2ee_project.utils.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Management", description = "APIs for managing user notifications")
public class NotificationController {

    private final NotificationServiceInterface notificationService;
    private final ResponseHandler responseHandler;
    private final RolePermissionUtils rolePermissionUtils;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public NotificationController(NotificationServiceInterface notificationService, ResponseHandler responseHandler,
                                 RolePermissionUtils rolePermissionUtils, JwtTokenProvider jwtTokenProvider) {
        this.notificationService = notificationService;
        this.responseHandler = responseHandler;
        this.rolePermissionUtils = rolePermissionUtils;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNotification(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody NotificationRequest request) {
//        if (!rolePermissionUtils.hasPermission(token, EPermission.CREATE_NOTIFICATION.getCode())) {
//            throw new ForbiddenException("Bạn không có quyền tạo thông báo!");
//        }
        NotificationDTO response = notificationService.createNotification(request);
        return responseHandler.responseCreated("Tạo thông báo thành công", response);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllNotifications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.VIEW_NOTIFICATION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem danh sách thông báo!");
        }
        Page<NotificationDTO> pageResponse = notificationService.getAllNotifications(page * size, size, search);
        return responseHandler.responseSuccess("Lấy danh sách thông báo thành công", pageResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id) {
//        if (!rolePermissionUtils.hasPermission(token, EPermission.VIEW_NOTIFICATION.getCode())) {
//            throw new ForbiddenException("Bạn không có quyền xem thông báo!");
//        }
        NotificationDTO response = notificationService.getNotificationById(id);
        return responseHandler.responseSuccess("Lấy thông tin thông báo thành công", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotification(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @Valid @RequestBody NotificationRequest request) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.UPDATE_NOTIFICATION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền cập nhật thông báo!");
        }
        NotificationDTO response = notificationService.updateNotification(id, request);
        return responseHandler.responseSuccess("Cập nhật thông báo thành công", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.DELETE_NOTIFICATION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xóa thông báo!");
        }
        notificationService.deleteNotification(id);
        return responseHandler.responseSuccess("Xóa thông báo thành công", null);
    }

    @PutMapping("/{id}/mark-as-read")
    public ResponseEntity<?> markAsRead(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.UPDATE_NOTIFICATION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền cập nhật thông báo!");
        }
        NotificationDTO response = notificationService.markAsRead(id);
        return responseHandler.responseSuccess("Đánh dấu đã đọc thành công", response);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyNotifications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Integer userId = jwtTokenProvider.getUserIdFromToken(token.replace("Bearer ", ""));
        Page<NotificationDTO> pageResponse = notificationService.getNotificationsByUserId(userId, page * size, size, search);
        return responseHandler.responseSuccess("Lấy danh sách thông báo của bạn thành công", pageResponse);
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastNotification(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody BroadcastNotificationRequest request) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.CREATE_NOTIFICATION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền gửi thông báo hàng loạt!");
        }
        notificationService.broadcastNotification(request);
        return responseHandler.responseSuccess("Gửi thông báo hàng loạt thành công", null);
    }
}