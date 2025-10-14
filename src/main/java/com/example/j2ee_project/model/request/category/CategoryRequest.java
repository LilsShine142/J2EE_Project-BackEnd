package com.example.j2ee_project.model.request.category;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 50, message = "Tên danh mục không được vượt quá 50 ký tự")
    private String categoryName;

    @Size(max = 100, message = "Mô tả không được vượt quá 100 ký tự")
    private String description;
}