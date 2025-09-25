package com.example.j2ee_project.model.request.bill;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BillRequestDTO {
    @NotNull(message = "UserID không được để trống")
    private Integer userID;

    @NotNull(message = "TableID không được để trống")
    private Integer tableID;

    @NotNull(message = "Ngày hóa đơn không được để trống")
    private LocalDate billDate;

    private String voucherCode;

    @Size(max = 20, message = "Phương thức thanh toán không được vượt quá 20 ký tự")
    private String paymentMethod;

    @DecimalMin(value = "0.0", message = "Thanh toán ban đầu không được âm")
    private Double initialPayment;
}