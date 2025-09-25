package com.example.j2ee_project.entity;

import com.example.j2ee_project.entity.keys.KeyBookingDetailId;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "bookingdetails")
public class BookingDetail {
    @EmbeddedId
    private KeyBookingDetailId id;

    @ManyToOne
    @MapsId("bookingID")
    @JoinColumn(name = "bookingid")
    private Booking booking;

    @ManyToOne
    @MapsId("mealID")
    @JoinColumn(name = "mealid")
    private Meal meal;

    @Column(name = "quantity")
    private Integer quantity = 1;

    public BookingDetail(Booking booking, Meal meal, Integer quantity) {
        this.id = new KeyBookingDetailId(booking.getBookingID(), meal.getMealID());
        this.booking = booking;
        this.meal = meal;
        this.quantity = quantity;
    }

    public BookingDetail() {
    }
}