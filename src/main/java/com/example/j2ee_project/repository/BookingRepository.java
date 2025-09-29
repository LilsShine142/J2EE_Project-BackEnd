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
}