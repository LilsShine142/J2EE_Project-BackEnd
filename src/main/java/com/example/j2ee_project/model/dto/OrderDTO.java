package com.example.j2ee_project.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Integer orderID;
    private Integer userID;
    private Integer bookingID;
    private String userName;
    private Integer tableID;
    private String tableName;
    private LocalDateTime orderDate;
    private String status;
    private Double totalAmount;
}