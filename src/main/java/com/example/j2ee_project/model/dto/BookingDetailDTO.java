package com.example.j2ee_project.model.dto;

import java.time.LocalDateTime;

import com.example.j2ee_project.entity.keys.KeyBookingDetailId;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BookingDetailDTO implements Serializable {
    private KeyBookingDetailId id;
    private Integer bookingID;
    private Integer mealID;
    // private String mealName;
    // private Double mealPrice;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}