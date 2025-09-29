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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookingid")
    private Integer bookingID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tableid")
    private RestaurantTable restaurantTable;

    @Column(name = "bookingdate", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "starttime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "endtime")
    private LocalDateTime endTime;

    @Column(name = "notes", length = 100)
    private String notes;

    @Column(name = "numberofguests", nullable = false)
    private Integer numberOfGuests;

    @Column(name = "initialpayment", precision = 10, scale = 2)
    private BigDecimal initialPayment = BigDecimal.ZERO;

    @Column(name = "paymentmethod", length = 20)
    private String paymentMethod;

    @Column(name = "paymenttime")
    private LocalDateTime paymentTime;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusid", nullable = false)
    private Status status; // FK đến bảng statuses

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BookingDetail> bookingDetails;
}
