package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @Column(name = "notificationid")
    private Integer notificationID;

    @ManyToOne
    @JoinColumn(name = "userid")
    private User user;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, length = 200)
    private String content;

    @Column(name = "sentdate")
    private LocalDateTime sentDate = LocalDateTime.now();

    @Column(name = "isread", length = 3)
    private String isRead = "No";
}