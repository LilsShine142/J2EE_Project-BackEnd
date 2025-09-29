package com.example.j2ee_project.model.request.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingRequestDTO {
    @NotNull(message = "UserID không được để trống")
    private Integer userID;

    @NotNull(message = "TableID không được để trống")
    private Integer tableID;

    @NotNull(message = "Ngày đặt không được để trống")
    private LocalDateTime bookingDate;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Size(max = 100, message = "Ghi chú không được vượt quá 100 ký tự")
    private String notes;

    @DecimalMin(value = "0.0", message = "Thanh toán ban đầu không được âm")
    private BigDecimal initialPayment;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    private LocalDateTime paymentTime;

    @NotNull(message = "Số lượng khách không được để trống")
    private Integer numberOfGuests;

    private List<BookingDetailRequest> meals;
}