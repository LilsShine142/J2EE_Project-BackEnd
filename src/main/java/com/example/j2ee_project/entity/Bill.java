package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billid")
    private Integer billID;

    @ManyToOne
    @JoinColumn(name = "userid")
    private User user;

    @ManyToOne
    @JoinColumn(name = "tableid")
    private RestaurantTable restaurantTable;

    @Column(name = "billdate", nullable = false)
    private LocalDate billDate;

    @Column(name = "mealtotal", precision = 10, scale = 2)
    private BigDecimal mealTotal = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "vouchercode")
    private Voucher voucher;

    @Column(name = "discountamount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "totalamount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paymentmethod", length = 20)
    private String paymentMethod;

    @Column(name = "paymenttime")
    private LocalDateTime paymentTime;

    @Column(name = "initialpayment", precision = 10, scale = 2)
    private BigDecimal initialPayment = BigDecimal.ZERO;

    @Column(name = "remainingamount", precision = 10, scale = 2)
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    @Column(name= "transactionno", length = 50)
    private String transactionNo; // Thêm trường để lưu vnp_TransactionNo

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "statusid", nullable = false)
    private Status status; // FK đến bảng statuses

    @OneToOne
    @JoinColumn(name = "orderid")
    private Order order; // Liên kết với Order

    @OneToOne
    @JoinColumn(name = "bookingid")
    private Booking booking; // Liên kết với Booking
}
