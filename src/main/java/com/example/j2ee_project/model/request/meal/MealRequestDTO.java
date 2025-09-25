package com.example.j2ee_project.model.request.meal;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MealRequestDTO {
    @NotBlank(message = "Tên món ăn không được để trống")
    @Size(max = 50, message = "Tên món ăn không được vượt quá 50 ký tự")
    private String mealName;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", message = "Giá không được âm")
    private Double price;

    private Integer categoryID;

    @Size(max = 20, message = "Trạng thái không được vượt quá 20 ký tự")
    private String status;
}