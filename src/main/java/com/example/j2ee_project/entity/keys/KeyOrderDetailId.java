package com.example.j2ee_project.entity.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyOrderDetailId implements Serializable {
    @Column(name = "orderid")
    private Integer orderID;

    @Column(name = "mealid")
    private Integer mealID;
}