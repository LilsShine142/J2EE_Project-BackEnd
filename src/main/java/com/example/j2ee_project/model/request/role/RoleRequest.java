package com.example.j2ee_project.model.request.role;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RoleRequest {
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(max = 20, message = "Tên vai trò không được vượt quá 20 ký tự")
    private String roleName;

    @Size(max = 100, message = "Mô tả không được vượt quá 100 ký tự")
    private String description;
}