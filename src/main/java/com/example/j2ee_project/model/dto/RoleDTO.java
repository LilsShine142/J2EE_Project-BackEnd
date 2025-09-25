package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class RoleDTO {
    private Integer roleID;
    private String roleName;
    private String description;
}