package com.example.j2ee_project.model.request.bill;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BillForBookingRequestDTO {

    @NotNull(message = "Số tiền thanh toán ban đầu là bắt buộc")
    private double initialPayment;

    @NotNull(message = "Phần trăm thanh toán là bắt buộc")
    private double paymentPercentage; // 30.0 hoặc 100.0

    private String voucherCode;

    private String orderInfo;
}