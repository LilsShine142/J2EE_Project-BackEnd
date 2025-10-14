package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.OrderDetail;
import com.example.j2ee_project.entity.keys.KeyOrderDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, KeyOrderDetailId> {
    @Modifying
    @Query("DELETE FROM OrderDetail od WHERE od.order.orderID = :orderId")
    void deleteByOrderId(@Param("orderId") Integer orderId);
}