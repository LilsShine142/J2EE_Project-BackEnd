package com.example.j2ee_project.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TableTypeDTO {
    private Integer tableTypeID;
    private String typeName;
    private Integer numberOfGuests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}