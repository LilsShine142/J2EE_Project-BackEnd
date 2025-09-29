package com.example.j2ee_project.utils._enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EStatus {
    ACTIVE(1, "Active"), // Bàn, món ăn, user đang hoạt động/sẵn sàng
    INACTIVE(2, "Inactive"), // Bàn bảo trì, món hết hàng, user bị khóa
    PENDING(3, "Pending"), // Booking/Order chờ xác nhận
    CONFIRMED(4, "Confirmed"), // Booking/Order đã xác nhận (thay vì Approved)
    CANCELLED(5, "Cancelled"), // Booking/Order bị hủy
    COMPLETED(6, "Completed"), // Booking/Order hoàn tất
    CHECKED_IN(7, "CheckedIn"), // Khách đã check-in cho Booking
    OCCUPIED(8, "Occupied"), // Bàn đang được sử dụng
    AVAILABLE(9, "Available"), // Bàn sẵn sàng (có thể dùng thay Active cho bàn)
    DELETED(10, "Deleted"), // Soft delete cho các bản ghi
    SUSPENDED(11, "Suspended"), // User bị đình chỉ tạm thời
    REJECTED(12, "Rejected"), // Booking/Order bị từ chối
    UNVERIFIED(13, "Unverified"), // Tài khoản chưa xác thực (users)
    VERIFIED(14, "Verified"); // Tài khoản đã xác thực (users)

    private final int code;
    private final String name;

    public static EStatus fromName(String name) {
        for (EStatus status : EStatus.values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy status với tên: " + name);
    }
}