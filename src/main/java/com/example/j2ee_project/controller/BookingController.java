package com.example.j2ee_project.controller;

import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.BookingDTO;
import com.example.j2ee_project.model.request.booking.BookingRequestDTO;
import com.example.j2ee_project.model.response.ResponseData;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.booking.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final ResponseHandler responseHandler;

    @Autowired
    public BookingController(BookingService bookingService, ResponseHandler responseHandler) {
        this.bookingService = bookingService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequestDTO bookingRequest) {
        try {
            BookingDTO response = bookingService.createBooking(bookingRequest);
            return responseHandler.responseCreated("Tạo đặt bàn thành công", response);
        } catch (IllegalStateException ex) {
            return responseHandler.responseError(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
            return responseHandler.responseError("Đã xảy ra lỗi khi tạo đặt bàn: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllBookings(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer tableId) {
        Page<BookingDTO> bookingPage = bookingService.getAllBookings(offset, limit, search, statusId, userId, tableId);
        return responseHandler.responseSuccess("Lấy danh sách booking thành công", bookingPage);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Integer bookingId) {
        BookingDTO response = bookingService.getBookingById(bookingId);
        return responseHandler.responseSuccess("Lấy thông tin booking thành công", response);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<?> updateBooking(@PathVariable Integer bookingId, @Valid @RequestBody BookingRequestDTO bookingRequestDTO) {
        BookingDTO response = bookingService.updateBooking(bookingId, bookingRequestDTO);
        return responseHandler.responseSuccess("Cập nhật booking thành công", response);
    }

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer bookingId) {
        try {
            ResponseData response = bookingService.cancelBooking(bookingId);
            return responseHandler.responseSuccess("Hủy booking thành công", response);
        } catch (IllegalStateException ex) {
            return responseHandler.responseError(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException ex) {
            return responseHandler.handleNotFound(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return responseHandler.handleServerError("Đã xảy ra lỗi khi hủy booking: " + ex.getMessage());
        }
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> deleteBooking(@PathVariable Integer bookingId) {
        bookingService.deleteBooking(bookingId);
        return responseHandler.responseSuccess("Hủy booking thành công", null);
    }
}