package com.example.j2ee_project.model.dto;

import com.example.j2ee_project.entity.keys.KeyBookingDetailId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderDetailDTO {
    private KeyBookingDetailId id;
    private Integer orderID;
    private Integer mealID;
//    private String mealName;
//    private Double mealPrice;
    private Integer quantity;
    private Double subTotal;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}