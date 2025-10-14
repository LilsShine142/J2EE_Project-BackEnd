package com.example.j2ee_project.model.request.log;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogRequest {
    @NotBlank(message = "Tên bảng không được để trống")
    @Size(max = 50, message = "Tên bảng không được vượt quá 50 ký tự")
    private String tableName;

    @NotNull(message = "ID bản ghi không được để trống")
    private Integer recordID;

    @Size(max = 20, message = "Hành động không được vượt quá 20 ký tự")
    private String action;

    @Size(max = 200, message = "Chi tiết thay đổi không được vượt quá 200 ký tự")
    private String changeDetails;

    private Integer userID; // Có thể null nếu không liên kết với user
}