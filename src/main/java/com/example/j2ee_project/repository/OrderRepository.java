package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Order;
import com.example.j2ee_project.entity.Status;

import jakarta.persistence.criteria.CriteriaBuilder.In;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

        // Tìm theo user ID (phân trang)
        Page<Order> findByUserUserID(Integer userID, Pageable pageable);

        // Tìm theo user ID (không phân trang)
        List<Order> findByUserUserID(Integer userID);

        // Tìm theo status (phân trang)
        Page<Order> findByStatus(Integer statusId, Pageable pageable);

        // Tìm theo status (không phân trang)
        List<Order> findByStatus(Status status);

        // Tìm theo khoảng thời gian
        Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

        // Tìm theo khoảng thời gian (không phân trang)
        List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

        // Tìm theo user và status
        Page<Order> findByUserUserIDAndStatus(Integer userID, Integer statusId, Pageable pageable);

        // Tìm theo user và khoảng thời gian
        Page<Order> findByUserUserIDAndOrderDateBetween(Integer userID, LocalDateTime startDate, LocalDateTime endDate,
                        Pageable pageable);

        // Tìm theo status và khoảng thời gian
        Page<Order> findByStatusAndOrderDateBetween(Integer statusId, LocalDateTime startDate, LocalDateTime endDate,
                        Pageable pageable);

        // Tìm theo user, status và khoảng thời gian
        Page<Order> findByUserUserIDAndStatusAndOrderDateBetween(Integer userID, Integer statusId,
                        LocalDateTime startDate,
                        LocalDateTime endDate, Pageable pageable);

        // Tính tổng doanh thu
        @Query("SELECT SUM(od.subTotal) FROM OrderDetail od WHERE od.order.orderDate BETWEEN :startDate AND :endDate")
        BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}