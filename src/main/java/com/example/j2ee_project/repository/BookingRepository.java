package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
        // Tìm booking theo user
        List<Booking> findByUser_UserID(Integer userID);

        // Tìm booking theo table
        List<Booking> findByRestaurantTable_TableID(Integer tableID);

        // Tìm booking theo status
        List<Booking> findByStatus_StatusID(Integer statusID);

        // Tìm booking theo ngày
        @Query("SELECT b FROM Booking b WHERE DATE(b.bookingDate) = DATE(:date)")
        List<Booking> findByBookingDate(@Param("date") LocalDateTime date);

        // Tìm booking trong khoảng thời gian
        @Query("SELECT b FROM Booking b WHERE b.startTime BETWEEN :startTime AND :endTime")
        List<Booking> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // Kiểm tra xem có booking nào trùng lặp không
        @Query("SELECT COUNT(b) > 0 FROM Booking b " +
                        "WHERE b.restaurantTable.tableID = :tableID " +
                        "AND b.status.statusName NOT IN :excludedStatuses " +
                        "AND b.startTime < :endTime " +
                        "AND b.endTime > :startTime")
        boolean existsOverlappingBooking(
                        @Param("tableID") Integer tableID,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("excludedStatuses") List<String> excludedStatuses);

        // Lấy các booking sắp tới của user
        @Query("SELECT b FROM Booking b WHERE b.user.userID = :userID " +
                        "AND b.startTime >= :currentTime ORDER BY b.startTime ASC")
        List<Booking> findUpcomingBookingsByUser(@Param("userID") Integer userID,
                        @Param("currentTime") LocalDateTime currentTime);

        // Lấy lịch sử booking của user
        @Query("SELECT b FROM Booking b WHERE b.user.userID = :userID " +
                        "AND b.startTime < :currentTime ORDER BY b.startTime DESC")
        List<Booking> findBookingHistoryByUser(@Param("userID") Integer userID,
                        @Param("currentTime") LocalDateTime currentTime);

        // Thống kê booking theo tháng
        @Query("SELECT MONTH(b.bookingDate) as month, COUNT(b) as total " +
                        "FROM Booking b WHERE YEAR(b.bookingDate) = :year " +
                        "GROUP BY MONTH(b.bookingDate)")
        List<Object[]> getBookingStatsByMonth(@Param("year") Integer year);

        // Tìm booking theo số điện thoại khách hàng
        @Query("SELECT b FROM Booking b WHERE b.user.phoneNumber = :phoneNumber")
        List<Booking> findByCustomerPhone(@Param("phoneNumber") String phoneNumber);

}