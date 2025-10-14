package com.example.j2ee_project.model.request.voucher;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRequest {
    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 50, message = "Mô tả không được vượt quá 50 ký tự")
    private String description;

    @NotNull(message = "Phần trăm giảm giá không được để trống")
    @Min(value = 0, message = "Phần trăm giảm giá không được nhỏ hơn 0")
    @Max(value = 100, message = "Phần trăm giảm giá không được lớn hơn 100")
    private Integer discountPercentage;

    @Size(max = 20, message = "Danh mục áp dụng không được vượt quá 20 ký tự")
    private String applicableCategory;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được nhỏ hơn 0")
    private Integer quantity;

    @NotNull(message = "Điểm yêu cầu không được để trống")
    @Min(value = 0, message = "Điểm yêu cầu không được nhỏ hơn 0")
    private Integer pointsRequired;
}