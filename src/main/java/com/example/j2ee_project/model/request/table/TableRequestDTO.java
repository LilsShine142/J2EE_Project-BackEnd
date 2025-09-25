package com.example.j2ee_project.model.request.table;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TableRequestDTO {
    @NotBlank(message = "Tên bàn không được để trống")
    @Size(max = 50, message = "Tên bàn không được vượt quá 50 ký tự")
    private String tableName;

    @NotBlank(message = "Vị trí không được để trống")
    @Size(max = 50, message = "Vị trí không được vượt quá 50 ký tự")
    private String location;

    @Size(max = 20, message = "Trạng thái không được vượt quá 20 ký tự")
    private String status;

    private Integer tableTypeID;
}