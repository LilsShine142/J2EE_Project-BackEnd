package com.example.j2ee_project.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BillDTO {
    private Integer billID;
    private Integer userID;
    private String userName;
    private Integer tableID;
    private String tableName;
    private Integer bookingID;
    private Integer orderID;
    private LocalDate billDate;
    private BigDecimal initialPayment;
    private BigDecimal totalAmount;
    private BigDecimal remainingAmount;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private Integer statusID;
    private String statusName;
    private String statusDescription;
    private String transactionNo; // Mã giao dịch từ cổng thanh toán
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double mealTotal;
    private String voucherCode;
    private String voucherDescription;
    private Double discountAmount;
//    private List<OrderDetailDTO> orderDetails; // Danh sách chi tiết món ăn
//    private List<BookingDetailDTO> bookingDetails; // Danh sách chi tiết đặt bàn (nếu có)
}