package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mealID")
    private Integer mealID;

    @Column(name = "mealname", nullable = false, length = 50)
    private String mealName;

    @Column(name = "price", nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "categoryid")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "statusid")
    private Status status;

    @OneToMany(mappedBy = "meal")
    private List<BookingDetail> bookingDetails;

    @OneToMany(mappedBy = "meal")
    private List<OrderDetail> orderDetails;
}
