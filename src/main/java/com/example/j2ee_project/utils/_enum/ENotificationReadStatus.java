package com.example.j2ee_project.utils._enum;

import lombok.Getter;

@Getter
public enum ENotificationReadStatus {
    YES("Yes", "Đã đọc"),
    NO("No", "Chưa đọc");

    private final String code;
    private final String description;

    ENotificationReadStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
