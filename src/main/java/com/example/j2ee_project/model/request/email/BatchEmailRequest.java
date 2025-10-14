package com.example.j2ee_project.model.request.email;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BatchEmailRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 50, message = "Tiêu đề không được vượt quá 50 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 200, message = "Nội dung không được vượt quá 200 ký tự")
    private String content;

    @NotBlank(message = "Loại template không được để trống")
    private String templateType; // "voucher", "new_dish", "promotion"
}