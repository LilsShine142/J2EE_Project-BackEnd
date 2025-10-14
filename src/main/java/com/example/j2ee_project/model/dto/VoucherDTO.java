package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class VoucherDTO {
    private Integer voucherid;
    private String voucherCode;
    private String description;
    private Integer discountPercentage;
    private String applicableCategory;
    private Integer quantity;
    private Integer pointsRequired;
}