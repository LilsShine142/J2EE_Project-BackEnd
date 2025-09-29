package com.example.j2ee_project.controller;

import com.example.j2ee_project.entity.RestaurantTable;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.table.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
public class TableController {
    private final TableService tableService;
    private final ResponseHandler responseHandler;

    @Autowired
    public TableController(TableService tableService, ResponseHandler responseHandler) {
        this.tableService = tableService;
        this.responseHandler = responseHandler;
    }

    // API lấy danh sách bàn trống với phân trang
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableTables(
            @RequestParam("bookingDate") LocalDateTime bookingDate,
            @RequestParam("startTime") LocalDateTime startTime,
            @RequestParam("endTime") LocalDateTime endTime,
            @RequestParam(value = "numberOfGuests", required = false) Integer numberOfGuests,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Object> result = tableService.getAvailableTables(bookingDate, startTime, endTime,
                    numberOfGuests, offset, limit);
            return responseHandler.responseSuccess("Danh sách bàn trống", result);
        } catch (Exception e) {
            e.printStackTrace();
            return responseHandler.responseError("Đã xảy ra lỗi khi lấy danh sách bàn trống",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
