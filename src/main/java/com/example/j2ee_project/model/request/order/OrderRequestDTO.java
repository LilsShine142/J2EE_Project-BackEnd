package com.example.j2ee_project.model.request.order;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {

    private Integer bookingID;

    @NotNull(message = "UserID không được để trống")
    private Integer userID;

    @NotNull(message = "TableID không được để trống")
    private Integer tableID;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    private List<OrderDetailRequest> orderDetails;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer statusId;
}

