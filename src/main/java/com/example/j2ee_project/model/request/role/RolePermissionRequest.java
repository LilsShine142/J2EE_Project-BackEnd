package com.example.j2ee_project.model.request.role;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RolePermissionRequest {
    @NotNull(message = "ID vai trò không được để trống")
    private Integer roleId;

    @NotNull(message = "ID quyền không được để trống")
    private Integer permissionId;

    @NotNull(message = "ID người cấp quyền không được để trống")
    private Integer grantedByUserId;
}