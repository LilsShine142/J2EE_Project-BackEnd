package com.example.j2ee_project.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDTO {
    private Integer userId;
    private Integer roleId;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String status; // Unverified / Active
    private String statusWork; // Tình trạng công việc
    private BigDecimal totalSpent;
    private Integer loyaltyPoints;
    private Date joinDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
