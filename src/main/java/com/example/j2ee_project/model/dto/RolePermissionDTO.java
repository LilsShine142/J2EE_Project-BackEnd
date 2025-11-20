package com.example.j2ee_project.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionDTO {
    private Integer roleId;
    private String roleName;
    private Integer permissionId;
    private String permissionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}