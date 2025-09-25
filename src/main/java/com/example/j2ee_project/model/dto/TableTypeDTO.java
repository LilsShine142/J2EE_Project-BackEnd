package com.example.j2ee_project.model.dto;

import lombok.Data;

@Data
public class TableTypeDTO {
    private Integer tableTypeID;
    private String typeName;
    private Integer capacity;
}