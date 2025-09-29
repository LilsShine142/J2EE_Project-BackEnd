package com.example.j2ee_project.model.request.booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingDetailRequest {
    @NotNull
    private Integer mealID;

    @NotNull
    @Min(1)
    private Integer quantity;

    
}