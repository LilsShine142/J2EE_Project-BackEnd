package com.example.j2ee_project.utils._enum;

import lombok.Getter;

@Getter
public enum EPermission {
    // User permissions
    VIEW_USER("VIEW_USER", "Xem danh sách người dùng"),
    CREATE_USER("CREATE_USER", "Tạo người dùng mới"),
    UPDATE_USER("UPDATE_USER", "Cập nhật thông tin người dùng"),
    DELETE_USER("DELETE_USER", "Xóa người dùng"),

    // Role permissions
    VIEW_ROLE("VIEW_ROLE", "Xem danh sách vai trò"),
    CREATE_ROLE("CREATE_ROLE", "Tạo vai trò mới"),
    UPDATE_ROLE("UPDATE_ROLE", "Cập nhật vai trò"),
    DELETE_ROLE("DELETE_ROLE", "Xóa vai trò"),

    // Permission permissions
    VIEW_PERMISSION("VIEW_PERMISSION", "Xem danh sách quyền"),
    CREATE_PERMISSION("CREATE_PERMISSION", "Tạo quyền mới"),
    UPDATE_PERMISSION("UPDATE_PERMISSION", "Cập nhật quyền"),
    DELETE_PERMISSION("DELETE_PERMISSION", "Xóa quyền"),

    // RolePermission permissions
    VIEW_ROLE_PERMISSION("VIEW_ROLE_PERMISSION", "Xem liên kết vai trò-quyền"),
    CREATE_ROLE_PERMISSION("CREATE_ROLE_PERMISSION", "Tạo liên kết vai trò-quyền"),
    UPDATE_ROLE_PERMISSION("UPDATE_ROLE_PERMISSION", "Cập nhật liên kết vai trò-quyền"),
    DELETE_ROLE_PERMISSION("DELETE_ROLE_PERMISSION", "Xóa liên kết vai trò-quyền"),

    // Category permissions
    VIEW_CATEGORY("VIEW_CATEGORY", "Xem danh sách danh mục"),
    CREATE_CATEGORY("CREATE_CATEGORY", "Tạo danh mục mới"),
    UPDATE_CATEGORY("UPDATE_CATEGORY", "Cập nhật danh mục"),
    DELETE_CATEGORY("DELETE_CATEGORY", "Xóa danh mục"),

    // Meal permissions
    VIEW_MEAL("VIEW_MEAL", "Xem danh sách món ăn"),
    CREATE_MEAL("CREATE_MEAL", "Tạo món ăn mới"),
    UPDATE_MEAL("UPDATE_MEAL", "Cập nhật món ăn"),
    DELETE_MEAL("DELETE_MEAL", "Xóa món ăn"),

    // Table permissions
    VIEW_TABLE("VIEW_TABLE", "Xem danh sách bàn"),
    CREATE_TABLE("CREATE_TABLE", "Tạo bàn mới"),
    UPDATE_TABLE("UPDATE_TABLE", "Cập nhật thông tin bàn"),
    DELETE_TABLE("DELETE_TABLE", "Xóa bàn"),

    // TableType permissions
    VIEW_TABLE_TYPE("VIEW_TABLE_TYPE", "Xem danh sách loại bàn"),
    CREATE_TABLE_TYPE("CREATE_TABLE_TYPE", "Tạo loại bàn mới"),
    UPDATE_TABLE_TYPE("UPDATE_TABLE_TYPE", "Cập nhật loại bàn"),
    DELETE_TABLE_TYPE("DELETE_TABLE_TYPE", "Xóa loại bàn"),

    // Booking permissions
    VIEW_BOOKING("VIEW_BOOKING", "Xem danh sách đặt bàn"),
    CREATE_BOOKING("CREATE_BOOKING", "Tạo đặt bàn mới"),
    UPDATE_BOOKING("UPDATE_BOOKING", "Cập nhật đặt bàn"),
    DELETE_BOOKING("DELETE_BOOKING", "Hủy đặt bàn"),

    // Order permissions
    VIEW_ORDER("VIEW_ORDER", "Xem danh sách đơn hàng"),
    CREATE_ORDER("CREATE_ORDER", "Tạo đơn hàng mới"),
    UPDATE_ORDER("UPDATE_ORDER", "Cập nhật đơn hàng"),
    DELETE_ORDER("DELETE_ORDER", "Hủy đơn hàng"),

    // Bill permissions
    VIEW_BILL("VIEW_BILL", "Xem danh sách hóa đơn"),
    CREATE_BILL("CREATE_BILL", "Tạo hóa đơn mới"),
    UPDATE_BILL("UPDATE_BILL", "Cập nhật hóa đơn"),
    DELETE_BILL("DELETE_BILL", "Xóa hóa đơn"),

    // Payment permissions
    VIEW_PAYMENT("VIEW_PAYMENT", "Xem danh sách thanh toán"),
    CREATE_PAYMENT("CREATE_PAYMENT", "Tạo thanh toán mới"),
    UPDATE_PAYMENT("UPDATE_PAYMENT", "Cập nhật thanh toán"),
    DELETE_PAYMENT("DELETE_PAYMENT", "Xóa thanh toán"),

    // Voucher permissions
    VIEW_VOUCHER("VIEW_VOUCHER", "Xem danh sách voucher"),
    CREATE_VOUCHER("CREATE_VOUCHER", "Tạo voucher mới"),
    UPDATE_VOUCHER("UPDATE_VOUCHER", "Cập nhật voucher"),
    DELETE_VOUCHER("DELETE_VOUCHER", "Xóa voucher"),

    // CustomerVoucher permissions
    VIEW_CUSTOMER_VOUCHER("VIEW_CUSTOMER_VOUCHER", "Xem voucher của khách hàng"),
    CREATE_CUSTOMER_VOUCHER("CREATE_CUSTOMER_VOUCHER", "Gán voucher cho khách hàng"),
    DELETE_CUSTOMER_VOUCHER("DELETE_CUSTOMER_VOUCHER", "Xóa voucher của khách hàng"),

    // Status permissions
    VIEW_STATUS("VIEW_STATUS", "Xem danh sách trạng thái"),
    CREATE_STATUS("CREATE_STATUS", "Tạo trạng thái mới"),
    UPDATE_STATUS("UPDATE_STATUS", "Cập nhật trạng thái"),
    DELETE_STATUS("DELETE_STATUS", "Xóa trạng thái"),

    // Notification permissions
    VIEW_NOTIFICATION("VIEW_NOTIFICATION", "Xem danh sách thông báo"),
    CREATE_NOTIFICATION("CREATE_NOTIFICATION", "Tạo thông báo mới"),
    UPDATE_NOTIFICATION("UPDATE_NOTIFICATION", "Cập nhật thông báo"),
    DELETE_NOTIFICATION("DELETE_NOTIFICATION", "Xóa thông báo"),

    // Profile
    VIEW_PROFILE("VIEW_PROFILE", "Xem thông tin profile cá nhân"),
    UPDATE_PROFILE("UPDATE_PROFILE", "Cập nhật thông tin profile cá nhân"),
    CHANGE_PASSWORD("CHANGE_PASSWORD", "Thay đổi mật khẩu cá nhân");

    private final String code;
    private final String description;

    EPermission(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
