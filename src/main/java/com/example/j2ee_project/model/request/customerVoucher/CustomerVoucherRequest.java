package com.example.j2ee_project.model.request.customerVoucher;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerVoucherRequest {
    @NotNull(message = "ID người dùng không được để trống")
    private Integer userID;

    @NotBlank(message = "Mã voucher không được để trống")
    private String voucherCode;

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 20, message = "Trạng thái không được vượt quá 20 ký tự")
    private String status;
}