package com.example.j2ee_project.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefundPaymentDTO {
    private String billID;    // billID để liên kết với Bill
    private long amount;      // Số tiền hoàn
    private String reason;    // Lý do hoàn tiền
    private String transactionNo; // Mã giao dịch ban đầu
    private String transactionType; // "02" full, "03" partial
    private LocalDateTime paymentTime; // Thời gian thanh toán ban đầu
}