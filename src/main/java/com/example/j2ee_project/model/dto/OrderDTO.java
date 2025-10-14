package com.example.j2ee_project.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDTO {
    private Integer orderID;
    private Integer userID;
    private Integer bookingID;
    private String userName;
    private Integer tableID;
    private String tableName;
    private LocalDateTime orderDate;
    private Integer statusId;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderDetailDTO> orderDetails;
}