package com.example.j2ee_project.model.request.email;

import com.example.j2ee_project.utils._enum.SendType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidEmailRequest
public class EmailRequest {

    @NotNull(message = "sendType là bắt buộc")
    private SendType sendType;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    private Map<String, Object> templateVariables;

    @Min(value = 1, message = "userId phải lớn hơn 0")
    private Integer userId;

    @Size(min = 1, message = "Danh sách userIds không được rỗng")
    private List<@Min(value = 1) Integer> userIds;

    @Min(value = 1, message = "roleId phải lớn hơn 0")
    private Integer roleId;
}