package com.example.j2ee_project.model.request.status;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StatusRequest {
    @NotBlank(message = "Tên trạng thái không được để trống")
    @Size(max = 20, message = "Tên trạng thái không được vượt quá 20 ký tự")
    private String statusName;

    @Size(max = 100, message = "Mô tả không được vượt quá 100 ký tự")
    private String description;

    private Integer code; // Có thể null
}