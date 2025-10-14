package com.example.j2ee_project.model.dto;

import com.example.j2ee_project.entity.keys.KeyBookingDetailId;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDTO {
    private KeyBookingDetailId id;
    private Integer orderID;
    private Integer mealID;
    private String mealName;
    private BigDecimal mealPrice;
    private Integer quantity;
    private BigDecimal subTotal;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}