package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class BookingDetailDTO {
    private Integer bookingID;
    private Integer mealID;
    private String mealName;
    private Double mealPrice;
    private Integer quantity;
}