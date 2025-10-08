package com.example.j2ee_project.model.request.table;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TableTypesRequest {
    @NotBlank(message = "Tên bàn không được để trống")
    @Size(max = 50, message = "Tên bàn không được vượt quá 50 ký tự")
    private String typeName;

    @NotNull(message = "Số lượng khách không được để trống")
    @Min(value = 1, message = "Số lượng khách phải lớn hơn 0")
    private Integer numberOfGuests;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}