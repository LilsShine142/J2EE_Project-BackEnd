package com.example.j2ee_project.controller;

import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.BookingDTO;
import com.example.j2ee_project.model.dto.BookingDetailDTO;
import com.example.j2ee_project.model.request.booking.BookingDetailRequest;
import com.example.j2ee_project.model.request.booking.BookingRequestDTO;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.booking.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // API tạo booking mới
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

    // API hủy booking
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer bookingId) {
        try {
            bookingService.cancelBooking(bookingId);
            return responseHandler.responseSuccess("Hủy đặt bàn thành công", null);
        } catch (ResourceNotFoundException ex) {
            return responseHandler.handleNotFound(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return responseHandler.responseError("Đã xảy ra lỗi khi hủy đặt bàn", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API thêm món ăn vào booking
    @PostMapping("/{bookingId}/details")
    public ResponseEntity<?> addBookingDetail(@PathVariable Integer bookingId,
            @Valid @RequestBody BookingDetailRequest request) {
        try {
            BookingDetailDTO response = bookingService.addBookingDetail(bookingId, request);
            return responseHandler.responseCreated("Thêm món ăn thành công", response);
        } catch (IllegalStateException ex) {
            return responseHandler.responseError(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
            return responseHandler.responseError("Đã xảy ra lỗi khi thêm món ăn", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API cập nhật món ăn trong booking
    @PutMapping("/details/{detailId}")
    public ResponseEntity<?> updateBookingDetail(@PathVariable Integer detailId,
            @Valid @RequestBody BookingDetailRequest request) {
        try {
            BookingDetailDTO response = bookingService.updateBookingDetail(detailId, request);
            if (response == null) {
                return responseHandler.responseSuccess("Xóa món ăn thành công", null);
            }
            return responseHandler.responseSuccess("Cập nhật món ăn thành công", response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return responseHandler.responseError("Đã xảy ra lỗi khi cập nhật món ăn", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API lấy danh sách món ăn trong booking
    @GetMapping("/{bookingId}/details")
    public ResponseEntity<?> getBookingDetails(@PathVariable Integer bookingId) {
        try {
            List<BookingDetailDTO> response = bookingService.getBookingDetails(bookingId);
            return responseHandler.responseSuccess("Lấy danh sách món ăn thành công", response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return responseHandler.responseError("Đã xảy ra lỗi khi lấy danh sách món ăn",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}