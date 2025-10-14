package com.example.j2ee_project.service.notification;

import com.example.j2ee_project.entity.Notification;
import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.NotificationDTO;
import com.example.j2ee_project.model.request.notification.NotificationRequest;
import com.example.j2ee_project.repository.NotificationRepository;
import com.example.j2ee_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationServiceInterface {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationDTO createNotification(NotificationRequest request) {
        User user = userRepository.findById(request.getUserID())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserID()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setSentDate(LocalDateTime.now());
        notification.setIsRead(request.getIsRead() != null ? request.getIsRead() : "No");

        Notification saved = notificationRepository.save(notification);
        return mapToNotificationDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getAllNotifications(int offset, int limit, String search) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Notification> page = notificationRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(search, search, pageable);
        return page.map(this::mapToNotificationDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + notificationId));
        return mapToNotificationDTO(notification);
    }

    @Override
    @Transactional
    public NotificationDTO updateNotification(Integer notificationId, NotificationRequest request) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + notificationId));

        User user = userRepository.findById(request.getUserID())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserID()));

        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setSentDate(LocalDateTime.now());
        notification.setIsRead(request.getIsRead() != null ? request.getIsRead() : "No");

        Notification updated = notificationRepository.save(notification);
        return mapToNotificationDTO(updated);
    }

    @Override
    @Transactional
    public void deleteNotification(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + notificationId));
        notificationRepository.delete(notification);
    }

    private NotificationDTO mapToNotificationDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationID(notification.getNotificationID());
        dto.setUserID(notification.getUser().getUserID());
        dto.setUserName(notification.getUser().getFullName());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setSentDate(notification.getSentDate());
        dto.setIsRead(notification.getIsRead());
        return dto;
    }
}