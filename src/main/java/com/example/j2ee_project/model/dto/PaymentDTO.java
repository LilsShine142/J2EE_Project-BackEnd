package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class PaymentDTO {
    private String billID;    // billID để liên kết với Bill
    private long amount;      // Số tiền thanh toán (VND)
    private String orderInfo; // Mô tả giao dịch
    private String paymentMethod; // Phương thức thanh toán (CASH, TRANSFER)
}