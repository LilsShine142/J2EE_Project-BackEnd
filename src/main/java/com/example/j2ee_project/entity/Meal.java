package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "meals")
public class Meal {
    @Id
    @Column(name = "mealid")
    private Integer mealID;

    @Column(name = "mealname", nullable = false, length = 50)
    private String mealName;

    @Column(name = "price", nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "status", length = 20)
    private String status;

    @ManyToOne
    @JoinColumn(name = "categoryid")
    private Category category;

    @OneToMany(mappedBy = "meal")
    private List<BookingDetail> bookingDetails;

    @OneToMany(mappedBy = "meal")
    private List<OrderDetail> orderDetails;
}
