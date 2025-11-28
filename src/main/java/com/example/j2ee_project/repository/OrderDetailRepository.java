package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.OrderDetail;
import com.example.j2ee_project.entity.keys.KeyOrderDetailId;
import com.example.j2ee_project.model.dto.MealDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import  com.example.j2ee_project.model.dto.MealDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, KeyOrderDetailId> {

    @Modifying
    @Query("DELETE FROM OrderDetail od WHERE od.order.orderID = :orderId")
    void deleteByOrderId(@Param("orderId") Integer orderId);

    @Query("""
        SELECT 
            od.meal.mealID AS mealID,
            od.meal.mealName AS mealName,
            od.meal.price AS price,
            od.meal.image AS image,
            od.meal.category.categoryID AS categoryID,
            od.meal.category.categoryName AS categoryName,
            od.meal.status.statusID AS statusId,
            COALESCE(SUM(od.quantity), 0) AS totalOrdered,
            od.meal.createdAt AS createdAt,
            od.meal.updatedAt AS updatedAt
        FROM OrderDetail od
        WHERE od.order.createdAt >= :startDate 
          AND od.order.createdAt <= :endDate
          AND od.order.status.statusID NOT IN (3,5,12)
        GROUP BY od.meal.mealID,
                 od.meal.mealName,
                 od.meal.price,
                 od.meal.image,
                 od.meal.category.categoryID,
                 od.meal.category.categoryName,
                 od.meal.status.statusID,
                 od.meal.createdAt,
                 od.meal.updatedAt
        ORDER BY SUM(od.quantity) DESC
        """)
    Page<TopMealStats> findTopMealsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT od.meal, SUM(od.quantity) FROM OrderDetail od WHERE od.order.createdAt >= :start AND od.order.createdAt <= :end AND od.order.status.statusID NOT IN (3,5,12) GROUP BY od.meal ORDER BY SUM(od.quantity) DESC")
    Page<Object[]> findTopOrderedMeals(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // Interface projection – đặt ngay trong file này cho tiện
    interface TopMealStats {
        Integer getMealID();
        String getMealName();
        Double getPrice();
        String getImage();
        Integer getCategoryID();
        String getCategoryName();
        Integer getStatusId();
        Long getTotalOrdered();         // SUM trả Long
        LocalDateTime getCreatedAt();
        LocalDateTime getUpdatedAt();
    }
}