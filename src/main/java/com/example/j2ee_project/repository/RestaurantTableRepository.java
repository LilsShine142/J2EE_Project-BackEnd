package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.RestaurantTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer> {
    boolean existsByTableName(String tableName);

    @Query("SELECT t FROM RestaurantTable t JOIN t.tableType tt WHERE " +
            "(:search IS NULL OR LOWER(t.tableName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:statusId IS NULL OR t.status.statusID = :statusId) AND " +
            "(:numberOfGuests IS NULL OR tt.numberOfGuests >= :numberOfGuests)")
    Page<RestaurantTable> findByFilters(
            @Param("search") String search,
            @Param("statusId") Integer statusId,
            @Param("numberOfGuests") Integer numberOfGuests,
            Pageable pageable);

    @Query("SELECT t FROM RestaurantTable t JOIN t.tableType tt WHERE " +
            "t.status.statusName = :status AND " +
            "tt.numberOfGuests >= :numberOfGuests AND " +
            "NOT EXISTS (SELECT b FROM Booking b WHERE b.restaurantTable = t AND " +
            "b.status.statusName NOT IN :excludedStatuses AND " +
            "(:startTime < b.endTime AND :endTime > b.startTime))")
    Page<RestaurantTable> findAvailableTables(
            @Param("status") String status,
            @Param("numberOfGuests") Integer numberOfGuests,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludedStatuses") List<String> excludedStatuses,
            Pageable pageable);
}