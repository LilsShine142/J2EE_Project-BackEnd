package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class PermissionDTO {
    private Integer permissionID;
    private String permissionName;
    private String description;
}