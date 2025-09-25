package com.example.j2ee_project.entity.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyBookingDetailId implements Serializable {
    @Column(name = "bookingid")
    private Integer bookingID;

    @Column(name = "mealid")
    private Integer mealID;
}