package com.example.j2ee_project.service.booking;

import com.example.j2ee_project.model.dto.BookingDTO;
import com.example.j2ee_project.model.request.booking.BookingRequestDTO;
import com.example.j2ee_project.model.response.ResponseData;
import org.springframework.data.domain.Page;

public interface BookingServiceInterface {
    BookingDTO createBooking(BookingRequestDTO bookingRequestDTO);

    Page<BookingDTO> getAllBookings(int offset, int limit, String search, Integer statusId, Integer userId, Integer tableId);

    BookingDTO getBookingById(Integer bookingId);

    BookingDTO updateBooking(Integer bookingId, BookingRequestDTO bookingRequestDTO);

    ResponseData cancelBooking(Integer bookingId) throws Exception;

    void deleteBooking(Integer bookingId);
}