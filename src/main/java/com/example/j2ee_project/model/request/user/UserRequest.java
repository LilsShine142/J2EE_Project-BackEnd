package com.example.j2ee_project.model.request.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserRequest {
    @NotNull(message = "RoleID là bắt buộc")
    private Integer roleId;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 50, message = "Email không được vượt quá 50 ký tự")
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 20, message = "Tên đăng nhập phải từ 3 đến 20 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 20, message = "Mật khẩu phải từ 6 đến 20 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 50, message = "Họ và tên không được vượt quá 50 ký tự")
    private String fullName;

    // @NotNull(message = "Ngày tham gia không được để trống")
    private LocalDateTime joinDate;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
    private String phoneNumber;

    // @Size(max = 10, message = "Mã xác minh không được vượt quá 10 ký tự")
    private String verifyCode;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer statusId;

    @DecimalMin(value = "0.0", message = "Tổng chi tiêu không được âm")
    private Double totalSpent = 0.0;

    @Min(value = 0, message = "Điểm thưởng không được âm")
    private Integer loyaltyPoints = 0;

    @NotBlank(message = "Trạng thái làm việc không được để trống")
    private String statusWork;

}