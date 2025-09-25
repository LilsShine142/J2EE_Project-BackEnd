package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logid")
    private Integer logID;

    @Column(name = "tablename", nullable = false, length = 50)
    private String tableName;

    @Column(name = "recordid", nullable = false)
    private Integer recordID;

    @Column(name = "action", length = 20)
    private String action;

    @Column(name = "changetime")
    private LocalDateTime changeTime = LocalDateTime.now();

    @Column(name = "changedetails", length = 200)
    private String changeDetails;

    @ManyToOne
    @JoinColumn(name = "userid")
    private User user;
}