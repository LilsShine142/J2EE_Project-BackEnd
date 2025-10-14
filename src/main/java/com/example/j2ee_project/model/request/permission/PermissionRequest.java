package com.example.j2ee_project.model.request.permission;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PermissionRequest {
    @NotBlank(message = "Tên quyền không được để trống")
    @Size(max = 50, message = "Tên quyền không được vượt quá 50 ký tự")
    private String permissionName;

    @Size(max = 100, message = "Mô tả không được vượt quá 100 ký tự")
    private String description;
}