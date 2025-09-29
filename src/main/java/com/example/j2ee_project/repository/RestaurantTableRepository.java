package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.RestaurantTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer> {

    @Query("SELECT t FROM RestaurantTable t " +
            "WHERE t.status.statusName = :availableStatus " +
            "AND (:numberOfGuests IS NULL OR t.tableType.capacity >= :numberOfGuests) " +
            "AND NOT EXISTS (" +
            "    SELECT b FROM Booking b " +
            "    WHERE b.restaurantTable.tableID = t.tableID " +
            "    AND b.status.statusName NOT IN :excludedStatuses " +
            "    AND b.startTime < :endTime " +
            "    AND b.endTime > :startTime" +
            ")")
    Page<RestaurantTable> findAvailableTables(
            @Param("availableStatus") String availableStatus,
            @Param("numberOfGuests") Integer numberOfGuests,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludedStatuses") List<String> excludedStatuses,
            Pageable pageable);
}
