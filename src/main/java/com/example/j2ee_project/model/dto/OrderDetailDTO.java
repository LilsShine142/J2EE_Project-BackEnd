package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class OrderDetailDTO {
    private Integer orderID;
    private Integer mealID;
    private String mealName;
    private Double mealPrice;
    private Integer quantity;
    private Double subTotal;
}