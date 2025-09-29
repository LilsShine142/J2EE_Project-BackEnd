package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid")
    private Integer userID;

    @Column(name = "roleid", nullable = false)
    private Integer roleId;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 20)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "verifycode", length = 10)
    private String verifyCode;

    @ManyToOne
    @JoinColumn(name = "statusid")
    private Status status; // FK đến bảng statuses

    @Column(name = "fullname", nullable = false, length = 50)
    private String fullName;

    @Column(name = "joindate", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "phonenumber", unique = true, length = 15)
    private String phoneNumber;

    @Column(name = "totalspent", precision = 10, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "loyaltypoints")
    private Integer loyaltyPoints = 0;

    @Column(name = "statuswork", length = 20)
    private String statusWork;

    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "user")
    private List<Bill> bills;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;
}
