package com.example.j2ee_project.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Integer notificationID;
    private Integer userID;
    private String userName;
    private String title;
    private String content;
    private LocalDateTime sentDate;
    private String isRead;
}