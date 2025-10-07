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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;


    @ManyToOne
    @JoinColumn(name = "statusid")
    private Status status;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;
}