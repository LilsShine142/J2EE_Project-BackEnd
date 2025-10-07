package com.example.j2ee_project.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingDTO {
    private Integer bookingID;
    private Integer userID;
    private String userName;  //Có thể bỏ trường này
    private Integer tableID;
    private String tableName;
    private LocalDateTime bookingDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer statusId;
    private String notes;
    private Integer numberOfGuests;
    private BigDecimal initialPayment;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paymentTime;
    private List<BookingDetailDTO> bookingDetails;
}