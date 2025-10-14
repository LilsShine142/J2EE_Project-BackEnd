package com.example.j2ee_project.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StatusDTO {
    private Integer statusID;
    private String statusName;
    private String description;
    private Integer code;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}