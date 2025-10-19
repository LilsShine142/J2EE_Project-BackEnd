package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.NotificationDTO;
import com.example.j2ee_project.model.request.notification.NotificationRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.notification.NotificationServiceInterface;
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

    @Autowired
    public NotificationController(NotificationServiceInterface notificationService, ResponseHandler responseHandler) {
        this.notificationService = notificationService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationDTO response = notificationService.createNotification(request);
        return responseHandler.responseCreated("Tạo thông báo thành công", response);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllNotifications(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<NotificationDTO> page = notificationService.getAllNotifications(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách thông báo thành công", page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(@PathVariable Integer id) {
        NotificationDTO response = notificationService.getNotificationById(id);
        return responseHandler.responseSuccess("Lấy thông tin thông báo thành công", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotification(@PathVariable Integer id, @Valid @RequestBody NotificationRequest request) {
        NotificationDTO response = notificationService.updateNotification(id, request);
        return responseHandler.responseSuccess("Cập nhật thông báo thành công", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        return responseHandler.responseSuccess("Xóa thông báo thành công", null);
    }
}