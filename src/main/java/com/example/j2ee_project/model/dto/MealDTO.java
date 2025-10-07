package com.example.j2ee_project.model.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealDTO {
    private Integer mealID;
    private String mealName;
    private Double price;
    private String image;
    private Integer categoryID;
    private String categoryName;
    private Integer statusId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}