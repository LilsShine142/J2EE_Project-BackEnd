package com.example.j2ee_project.model.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTableDTO {
    private Integer tableID;
    private String tableName;
    private String location;
    private Integer statusId;
    private Integer tableTypeID;
    private String tableTypeName;
    private Integer numberOfGuests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}