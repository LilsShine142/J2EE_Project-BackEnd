package com.example.j2ee_project.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoleDTO {
    private Integer roleID;
    private String roleName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}