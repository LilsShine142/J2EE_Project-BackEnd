package com.example.j2ee_project.service.booking;

import com.example.j2ee_project.entity.RestaurantTable;
import com.example.j2ee_project.model.dto.BookingDTO;
import com.example.j2ee_project.model.dto.BookingDetailDTO;
import com.example.j2ee_project.model.request.booking.BookingDetailRequest;
import com.example.j2ee_project.model.request.booking.BookingRequestDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingServiceInterface {

    BookingDTO createBooking(BookingRequestDTO bookingRequest);

    void cancelBooking(Integer bookingID);

    BookingDetailDTO addBookingDetail(Integer bookingID, BookingDetailRequest request);

    BookingDetailDTO updateBookingDetail(Integer detailID, BookingDetailRequest request);

    List<BookingDetailDTO> getBookingDetails(Integer bookingID);
    
    
}
