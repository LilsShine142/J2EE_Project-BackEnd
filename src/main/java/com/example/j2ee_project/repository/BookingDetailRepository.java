package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.BookingDetail;
import com.example.j2ee_project.entity.keys.KeyBookingDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, KeyBookingDetailId> {
    @Modifying
    @Query("DELETE FROM BookingDetail bd WHERE bd.booking.bookingID = :bookingId")
    void deleteByBookingId(@Param("bookingId") Integer bookingId);
}