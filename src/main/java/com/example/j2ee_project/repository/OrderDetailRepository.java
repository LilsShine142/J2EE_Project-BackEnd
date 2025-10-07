package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // Tìm order details theo order ID
    List<OrderDetail> findByOrderOrderID(Integer orderID);

    // Xóa order details theo order ID
    void deleteByOrderOrderID(Integer orderID);

    Optional<OrderDetail> findByOrderOrderIDAndMealMealID(Integer orderID, Integer mealID);

    // Tính tổng tiền của order
    @Query("SELECT SUM(od.subTotal) FROM OrderDetail od WHERE od.order.orderID = :orderID")
    BigDecimal sumSubTotalByOrderOrderID(@Param("orderID") Integer orderID);
}