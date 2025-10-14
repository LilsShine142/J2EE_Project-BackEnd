package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillRepository extends JpaRepository<Bill, Integer> {
    @Query("SELECT b FROM Bill b WHERE b.booking.bookingID = :bookingId")
    Bill findByBookingId(@Param("bookingId") Integer bookingId);

    @Query("SELECT b FROM Bill b WHERE b.order.orderID = :orderId")
    Bill findByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT b FROM Bill b WHERE " +
            "(:search IS NULL OR :search = '' OR b.paymentMethod LIKE %:search%) " +
            "AND (:statusId IS NULL OR b.status.statusID = :statusId) " +
            "AND (:userId IS NULL OR b.user.userID = :userId) " +
            "AND (:tableId IS NULL OR b.restaurantTable.tableID = :tableId)")
    Page<Bill> findByFilters(
            @Param("search") String search,
            @Param("statusId") Integer statusId,
            @Param("userId") Integer userId,
            @Param("tableId") Integer tableId,
            Pageable pageable);


}