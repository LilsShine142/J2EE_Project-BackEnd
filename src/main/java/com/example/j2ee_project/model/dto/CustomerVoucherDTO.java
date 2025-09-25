package com.example.j2ee_project.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerVoucherDTO {
    private Integer customerVoucherID;
    private Integer userID;
    private String userName;
    private String voucherCode;
    private String voucherDescription;
    private Integer discountPercentage;
    private LocalDateTime receivedDate;
    private String status;
}