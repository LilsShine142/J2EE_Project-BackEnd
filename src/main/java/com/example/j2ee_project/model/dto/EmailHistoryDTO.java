package com.example.j2ee_project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailHistoryDTO {
    private Integer id;
    private Integer userId;
    private String email;
    private String title;
    private String content;
    private String sendType;
    private LocalDateTime sentAt;
    private String status;
}
