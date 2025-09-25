package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @Column(name = "bookingid")
    private Integer bookingID;

    @ManyToOne
    @JoinColumn(name = "userid")
    private User user;

    @ManyToOne
    @JoinColumn(name = "tableid")
    private RestaurantTable table;

    @Column(name = "bookingdate", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "starttime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "endtime")
    private LocalDateTime endTime;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "notes", length = 100)
    private String notes;

    @Column(name = "initialpayment", precision = 10, scale = 2)
    private BigDecimal initialPayment = BigDecimal.ZERO;

    @Column(name = "paymentmethod", length = 20)
    private String paymentMethod;

    @Column(name = "paymenttime")
    private LocalDateTime paymentTime;

    @OneToMany(mappedBy = "booking")
    private List<BookingDetail> bookingDetails;
}
