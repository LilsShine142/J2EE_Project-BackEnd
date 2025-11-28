package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
        @Query("SELECT b FROM Booking b WHERE " +
                "(:search IS NULL OR LOWER(b.notes) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                "(:statusId IS NULL OR b.status.statusID = :statusId) AND " +
                "(:userId IS NULL OR b.user.userID = :userId) AND " +
                "(:tableId IS NULL OR b.restaurantTable.tableID = :tableId)")
        Page<Booking> findByFilters(
                @Param("search") String search,
                @Param("statusId") Integer statusId,
                @Param("userId") Integer userId,
                @Param("tableId") Integer tableId,
                Pageable pageable);

        @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE " +
                "b.restaurantTable.tableID = :tableId AND " +
                "b.status.statusName NOT IN :excludedStatuses AND " +
                "(:startTime < b.endTime AND :endTime > b.startTime)")
        boolean existsOverlappingBooking(
                @Param("tableId") Integer tableId,
                @Param("startTime") LocalDateTime startTime,
                @Param("endTime") LocalDateTime endTime,
                @Param("excludedStatuses") List<String> excludedStatuses);

        // Thống kê theo ngày
        @Query("SELECT FUNCTION('DATE', b.createdAt) AS period, COUNT(b) AS count FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate AND b.status.statusID NOT IN (3,5,12) GROUP BY FUNCTION('DATE', b.createdAt) ORDER BY period DESC")
        org.springframework.data.domain.Page<java.lang.Object[]> getBookingStatsByDay(
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate,
                org.springframework.data.domain.Pageable pageable);

        // Thống kê theo tháng
        @Query("SELECT FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m') AS period, COUNT(b) AS count FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate AND b.status.statusID NOT IN (3,5,12) GROUP BY FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m') ORDER BY period DESC")
        org.springframework.data.domain.Page<java.lang.Object[]> getBookingStatsByMonth(
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate,
                org.springframework.data.domain.Pageable pageable);

        // Đếm theo khoảng thời gian (dùng trong overview)
        @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :start AND b.createdAt <= :end AND b.status.statusID NOT IN (3,5,12)")
        long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.userID = :userId AND b.createdAt >= :startDate AND b.createdAt <= :endDate AND b.status.statusID NOT IN (3,5,12)")
        long countByUserIdAndDateRange(@Param("userId") Integer userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}