package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Entity
@Getter
@Setter
@Table(name = "statuses")
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statusid")
    private Integer statusID;

    @Column(name = "statusname", nullable = false, unique = true, length = 20)
    private String statusName;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "code")
    private Integer code;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}