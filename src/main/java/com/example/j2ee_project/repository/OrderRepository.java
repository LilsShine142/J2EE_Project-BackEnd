package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByBookingID(Integer bookingID);

    @Query("SELECT o FROM Order o WHERE " +
            "(:search IS NULL OR LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:statusId IS NULL OR o.status.statusID = :statusId) AND " +
            "(:userId IS NULL OR o.user.userID = :userId) AND " +
            "(:tableId IS NULL OR o.restaurantTable.tableID = :tableId)")
    Page<Order> findByFilters(
            @Param("search") String search,
            @Param("statusId") Integer statusId,
            @Param("userId") Integer userId,
            @Param("tableId") Integer tableId,
            Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate AND o.status.statusID NOT IN (3,5,12)")
    long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.user.userID = :userId AND o.createdAt >= :startDate AND o.createdAt <= :endDate AND o.status.statusID NOT IN (3,5,12)")
    List<Order> findByUserIdAndDateRange(@Param("userId") Integer userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.userID = :userId AND o.createdAt >= :startDate AND o.createdAt <= :endDate AND o.status.statusID NOT IN (3,5,12)")
    long countByUserIdAndDateRange(@Param("userId") Integer userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT o.user.userID) FROM Order o WHERE o.createdAt >= :start AND o.createdAt <= :end AND o.status.statusID NOT IN (3,5,12)")
    long countDistinctCustomersInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}