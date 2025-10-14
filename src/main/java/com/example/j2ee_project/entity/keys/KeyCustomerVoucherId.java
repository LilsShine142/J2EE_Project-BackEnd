package com.example.j2ee_project.entity.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyCustomerVoucherId implements Serializable {
    @Column(name = "userid")
    private Integer userID;

    @Column(name = "voucherid")
    private Integer voucherId;
}