package com.example.j2ee_project.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogDTO {
    private Integer logID;
    private String tableName;
    private Integer recordID;
    private String action;
    private LocalDateTime changeTime;
    private String changeDetails;
    private Integer userID;
    private String userName;
}