package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@Entity
@Table(name = "tabletypes")
public class TableType {
    @Id
    @Column(name = "tabletypeid")
    private Integer tableTypeID;

    @Column(name = "typename", nullable = false, length = 50)
    private String typeName;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @OneToMany(mappedBy = "tableType")
    private List<RestaurantTable> tables;
}