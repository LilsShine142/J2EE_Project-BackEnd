package com.example.j2ee_project.entity;

import com.example.j2ee_project.entity.keys.KeyOrderDetailId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "orderdetails")
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

    public OrderDetail(Order order, Meal meal, Integer quantity, BigDecimal subTotal) {
        this.id = new KeyOrderDetailId(order.getOrderID(), meal.getMealID());
        this.order = order;
        this.meal = meal;
        this.quantity = quantity;
        this.subTotal = subTotal;
    }

    public OrderDetail() {
    }

    // Tính subtotal tự động
    @PrePersist
    @PreUpdate
    private void calculateSubTotal() {
        if (meal != null && quantity != null) {
            this.subTotal = meal.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
