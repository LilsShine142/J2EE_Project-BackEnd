package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "status", length = 20)
    private String status;

    @ManyToOne
    @JoinColumn(name = "tabletypeid")
    private TableType tableType;

    @OneToMany(mappedBy = "table")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "table")
    private List<Bill> bills;

    @OneToMany(mappedBy = "table")
    private List<Order> orders;
}