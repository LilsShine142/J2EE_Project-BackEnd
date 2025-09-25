package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@Entity
@Table(name = "vouchers")
public class Voucher {
    @Id
    @Column(name = "vouchercode", length = 10)
    private String voucherCode;

    @Column(name = "description", nullable = false, length = 50)
    private String description;

    @Column(name = "discountpercentage")
    private Integer discountPercentage;

    @Column(name = "applicablecategory", length = 20)
    private String applicableCategory;

    @Column(name = "quantity")
    private Integer quantity = 0;

    @Column(name = "pointsrequired")
    private Integer pointsRequired = 0;

    @OneToMany(mappedBy = "voucher")
    private List<Bill> bills;

    @OneToMany(mappedBy = "voucher")
    private List<CustomerVoucher> customerVouchers;
}