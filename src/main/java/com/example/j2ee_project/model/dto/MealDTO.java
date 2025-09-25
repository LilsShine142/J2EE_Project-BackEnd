package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class MealDTO {
    private Integer mealID;
    private String mealName;
    private Double price;
    private Integer categoryID;
    private String categoryName;
    private String status;
}