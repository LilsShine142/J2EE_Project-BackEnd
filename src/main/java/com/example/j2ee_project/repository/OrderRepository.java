package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Integer> {
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
}