package com.example.j2ee_project.entity;

import com.example.j2ee_project.entity.keys.KeyOrderDetailId;
import com.example.j2ee_project.model.dto.BillDTO;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orderdetails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {
    @EmbeddedId
    private KeyOrderDetailId id;

    @ManyToOne
    @MapsId("orderID")
    @JoinColumn(name = "orderid")
    private Order order;

    @ManyToOne
    @MapsId("mealID")
    @JoinColumn(name = "mealid")
    private Meal meal;

    @Column(name = "quantity")
    private Integer quantity = 1;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "createdat")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updatedat")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Auto calculate subtotal
    @PrePersist
    @PreUpdate
    public void calculateSubTotal() {
        if (meal != null && quantity != null) {
            this.subTotal = meal.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
