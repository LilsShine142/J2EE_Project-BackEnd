package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "tables")
public class RestaurantTable {
    @Id
    @Column(name = "tableid")
    private Integer tableID;

    @Column(name = "tablename", nullable = false, length = 50)
    private String tableName;

    @Column(name = "location", nullable = false, length = 50)
    private String location;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "statusid", nullable = false)
    private Status status; // FK đến bảng statuses

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabletypeid")
    private TableType tableType;

    @OneToMany(mappedBy = "restaurantTable", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "restaurantTable", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Bill> bills;

    @OneToMany(mappedBy = "restaurantTable", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders;
}