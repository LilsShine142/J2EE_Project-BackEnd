package com.example.j2ee_project.entity;

import java.time.LocalDateTime;

import com.example.j2ee_project.entity.keys.KeyBookingDetailId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bookingdetails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDetail {
    @EmbeddedId
    private KeyBookingDetailId id;

    @ManyToOne
    @MapsId("bookingID")
    @JoinColumn(name = "bookingid", insertable = false, updatable = false)
    private Booking booking;

    @ManyToOne
    @MapsId("mealID")
    @JoinColumn(name = "mealid", insertable = false, updatable = false)
    private Meal meal;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

}