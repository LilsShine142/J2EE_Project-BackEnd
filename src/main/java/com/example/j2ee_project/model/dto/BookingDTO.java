package com.example.j2ee_project.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDTO {
    private Integer bookingID;
    private Integer userID;
    private String userName;
    private Integer tableID;
    private String tableName;
    private LocalDateTime bookingDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String notes;
    private Integer numberOfGuests;
    private BigDecimal initialPayment;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paymentTime;
}