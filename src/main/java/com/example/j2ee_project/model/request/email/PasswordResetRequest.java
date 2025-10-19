package com.example.j2ee_project.model.request.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}