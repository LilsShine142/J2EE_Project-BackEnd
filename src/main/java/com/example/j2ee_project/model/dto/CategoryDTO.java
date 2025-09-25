package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Integer categoryID;
    private String categoryName;
    private String description;
}