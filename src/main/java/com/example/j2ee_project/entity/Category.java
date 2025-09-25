package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @Column(name = "categoryid")
    private Integer categoryID;

    @Column(name = "categoryname", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "description", length = 100)
    private String description;

    @OneToMany(mappedBy = "category")
    private List<Meal> meals;
}