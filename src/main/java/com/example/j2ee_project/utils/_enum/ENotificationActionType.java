package com.example.j2ee_project.utils._enum;

import lombok.Getter;

@Getter
public enum ENotificationActionType {
    NONE("NONE", "Không có hành động"),
    VIEW_MEAL("VIEW_MEAL", "Xem món ăn"),
    VIEW_BOOKING("VIEW_BOOKING", "Xem đặt bàn"),
    VIEW_CATEGORY("VIEW_CATEGORY", "Xem danh mục"),
    VIEW_CART("VIEW_CART", "Xem giỏ hàng"),
    WELCOME("WELCOME", "Chào mừng"),
    PROMO_CODE("PROMO_CODE", "Mã giảm giá"),
    THANK_YOU("THANK_YOU", "Cảm ơn");

    private final String code;
    private final String description;

    ENotificationActionType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
