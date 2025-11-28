package com.example.j2ee_project.model.request.notification;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastNotificationRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 50, message = "Tiêu đề không được vượt quá 50 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 200, message = "Nội dung không được vượt quá 200 ký tự")
    private String content;

    @Pattern(regexp = "^(NONE|VIEW_MEAL|VIEW_BOOKING|VIEW_CATEGORY|VIEW_CART|WELCOME|PROMO_CODE|THANK_YOU)$", message = "Loại hành động không hợp lệ")
    private String actionType; // Loại hành động, mặc định "NONE"

    private Integer actionId; // ID liên quan đến hành động, có thể null

    private List<Integer> userIds; // Danh sách ID người dùng nhận thông báo, nếu null hoặc rỗng thì gửi cho tất cả
}
