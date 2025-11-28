package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Integer> {
    @Query("SELECT b FROM Bill b WHERE b.booking.bookingID = :bookingId")
    Bill findByBookingId(@Param("bookingId") Integer bookingId);

    @Query("SELECT b FROM Bill b WHERE b.order.orderID = :orderId")
    Bill findByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT b FROM Bill b WHERE " +
            "(:search IS NULL OR :search = '' OR b.paymentMethod LIKE %:search%) " +
            "AND (:statusId IS NULL OR b.status.statusID = :statusId) " +
            "AND (:userId IS NULL OR b.user.userID = :userId) " +
            "AND (:tableId IS NULL OR b.restaurantTable.tableID = :tableId)")
    Page<Bill> findByFilters(
            @Param("search") String search,
            @Param("statusId") Integer statusId,
            @Param("userId") Integer userId,
            @Param("tableId") Integer tableId,
            Pageable pageable);

    // Thống kê hóa đơn theo ngày
    @Query("SELECT FUNCTION('DATE', b.createdAt) AS period, COUNT(b) AS count, COALESCE(SUM(b.totalAmount), 0) AS revenue FROM Bill b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate GROUP BY FUNCTION('DATE', b.createdAt) ORDER BY period DESC")
    org.springframework.data.domain.Page<java.lang.Object[]> getBillStatsByDay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);

    // Thống kê hóa đơn theo tháng
    @Query("SELECT FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m') AS period, COUNT(b) AS count, COALESCE(SUM(b.totalAmount), 0) AS revenue FROM Bill b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate GROUP BY FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m') ORDER BY period DESC")
    org.springframework.data.domain.Page<java.lang.Object[]> getBillStatsByMonth(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);

    // Tổng doanh thu
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.createdAt >= :start AND b.createdAt <= :end")
    BigDecimal sumTotalAmountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Doanh thu trung bình mỗi hóa đơn
    @Query("SELECT COALESCE(AVG(b.totalAmount), 0) FROM Bill b WHERE b.createdAt >= :start AND b.createdAt <= :end")
    BigDecimal averageAmountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Đếm hóa đơn
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.createdAt >= :start AND b.createdAt <= :end")
    long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Bill b WHERE b.user.userID = :userId AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    java.util.List<Bill> findByUserIdAndDateRange(@Param("userId") Integer userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.user.userID = :userId AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    long countByUserIdAndDateRange(@Param("userId") Integer userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Chi tiêu trung bình của khách hàng
    @Query("SELECT AVG(b.totalAmount) FROM Bill b WHERE b.createdAt >= :start AND b.createdAt <= :end")
    BigDecimal avgSpendPerCustomer(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Đếm số lượng khách hàng đã sử dụng voucher
    @Query("SELECT COUNT(DISTINCT b.user.userID) FROM Bill b WHERE b.voucher IS NOT NULL AND b.createdAt >= :start AND b.createdAt <= :end")
    long countCustomersUsedVoucher(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Tổng số tiền giảm giá
    @Query("SELECT COALESCE(SUM(b.discountAmount), 0) FROM Bill b WHERE b.createdAt >= :start AND b.createdAt <= :end")
    BigDecimal sumDiscountAmount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Khách hàng hàng đầu
    @Query("SELECT b.user, SUM(b.totalAmount) FROM Bill b WHERE b.createdAt >= :start AND b.createdAt <= :end GROUP BY b.user ORDER BY SUM(b.totalAmount) DESC")
    org.springframework.data.domain.Page<java.lang.Object[]> findTopCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, org.springframework.data.domain.Pageable pageable);

    // Thống kê sử dụng voucher
    @Query("SELECT v.voucherCode, COUNT(b) FROM Bill b JOIN b.voucher v WHERE b.createdAt >= :start AND b.createdAt <= :end GROUP BY v.voucherCode ORDER BY COUNT(b) DESC")
    java.util.List<java.lang.Object[]> getVoucherUsageStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}