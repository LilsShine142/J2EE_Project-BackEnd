package com.example.j2ee_project.service.notification;

import com.example.j2ee_project.model.dto.NotificationDTO;
import com.example.j2ee_project.model.request.notification.NotificationRequest;
import org.springframework.data.domain.Page;

public interface NotificationServiceInterface {
    NotificationDTO createNotification(NotificationRequest request);

    Page<NotificationDTO> getAllNotifications(int offset, int limit, String search);

    NotificationDTO getNotificationById(Integer notificationId);

    NotificationDTO updateNotification(Integer notificationId, NotificationRequest request);

    void deleteNotification(Integer notificationId);
}