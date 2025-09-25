package com.example.j2ee_project.model.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BillDTO {
    private Integer billID;
    private Integer userID;
    private String userName;
    private Integer tableID;
    private String tableName;
    private LocalDate billDate;
    private Double mealTotal;
    private String voucherCode;
    private String voucherDescription;
    private Double discountAmount;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private Double initialPayment;
    private Double remainingAmount;
}