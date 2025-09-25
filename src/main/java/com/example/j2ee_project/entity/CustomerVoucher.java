package com.example.j2ee_project.entity;

import com.example.j2ee_project.entity.keys.KeyCustomerVoucherId;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customervouchers")
public class CustomerVoucher {
    @EmbeddedId
    private KeyCustomerVoucherId id;

    @ManyToOne
    @MapsId("userID")
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("voucherCode")
    @JoinColumn(name = "vouchercode", nullable = false)
    private Voucher voucher;

    @Column(name = "receiveddate")
    private LocalDateTime receivedDate = LocalDateTime.now();

    @Column(name = "status", length = 20)
    private String status;

    public CustomerVoucher(User user, Voucher voucher, String status) {
        this.id = new KeyCustomerVoucherId(user.getUserID(), voucher.getVoucherCode());
        this.user = user;
        this.voucher = voucher;
        this.status = status;
    }

    public CustomerVoucher() {
    }
}