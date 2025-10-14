package com.example.j2ee_project.model.request.notification;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    @NotNull(message = "ID người dùng không được để trống")
    private Integer userID;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 50, message = "Tiêu đề không được vượt quá 50 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 200, message = "Nội dung không được vượt quá 200 ký tự")
    private String content;

    @Size(max = 3, message = "Trạng thái đọc không được vượt quá 3 ký tự")
    private String isRead; // "Yes" hoặc "No", mặc định "No"
}