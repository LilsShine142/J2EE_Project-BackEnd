package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class RestaurantTableDTO {
    private Integer tableID;
    private String tableName;
    private String location;
    private String status;
    private Integer tableTypeID;
    private String tableTypeName;
    private Integer capacity;
}