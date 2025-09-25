package com.example.j2ee_project.model.request.order;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    @NotNull(message = "UserID không được để trống")
    private Integer userID;

    @NotNull(message = "TableID không được để trống")
    private Integer tableID;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    private List<OrderItemDTO> orderItems;

    @Size(max = 20, message = "Trạng thái không được vượt quá 20 ký tự")
    private String status;
}

@Data
class OrderItemDTO {
    @NotNull(message = "MealID không được để trống")
    private Integer mealID;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
}