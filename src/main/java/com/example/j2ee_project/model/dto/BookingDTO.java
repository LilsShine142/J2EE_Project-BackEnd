package com.example.j2ee_project.model.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Integer bookingID;
    private Integer userID;
    private String userName;
    private Integer tableID;
    private String tableName;
    private LocalDate bookingDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String notes;
    private Double initialPayment;
    private String paymentMethod;
    private LocalDateTime paymentTime;
}