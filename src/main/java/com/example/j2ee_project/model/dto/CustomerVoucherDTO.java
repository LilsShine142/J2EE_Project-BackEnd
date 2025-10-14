package com.example.j2ee_project.model.dto;

import com.example.j2ee_project.entity.keys.KeyCustomerVoucherId;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerVoucherDTO {
    private KeyCustomerVoucherId id;
    private Integer voucherId;
    private Integer userID;
    private String userName;
    private String voucherCode;
    private String voucherDescription;
    private Integer discountPercentage;
    private LocalDateTime receivedDate;
    private String status;
}