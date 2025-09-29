package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.BookingDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Integer> {
    List<BookingDetail> findByBookingBookingID(Integer bookingID);

    void deleteByBookingBookingID(Integer bookingID); // Nếu cần xóa all khi hủy booking

    // Tìm booking detail bằng bookingId và mealId
    Optional<BookingDetail> findByBookingBookingIDAndMealMealID(Integer bookingId, Integer mealId);
}