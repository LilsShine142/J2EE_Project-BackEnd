package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "orderid")
    private Integer orderID;

    @ManyToOne
    @JoinColumn(name = "userid")
    private User user;

    @Column(name = "bookingid", nullable = true)
    private Integer bookingID;

    @ManyToOne
    @JoinColumn(name = "tableid")
    private RestaurantTable restaurantTable;

    @Column(name = "orderdate")
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "status", length = 20)
    private String status;

    @OneToMany(mappedBy = "order")
    private List<OrderDetail> orderDetails;
}