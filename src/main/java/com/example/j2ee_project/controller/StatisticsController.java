package com.example.j2ee_project.controller;

import com.example.j2ee_project.exception.ForbiddenException;
import com.example.j2ee_project.model.dto.MealDTO;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.statistics.StatisticsServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsServiceInterface statisticsService;
    private final ResponseHandler responseHandler;

    // Helper method để extract token
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ForbiddenException("Thiếu token xác thực");
        }
        return authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            String authToken = extractToken(token);
            Map<String, Object> stats = statisticsService.getStatistics(startDate, endDate);
            return responseHandler.responseSuccess("Lấy thống kê tổng quan thành công", stats);
        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return responseHandler.handleServerError("Đã xảy ra lỗi khi lấy thống kê tổng quan");
        }
    }

    @GetMapping("/top-meals")
    public ResponseEntity<?> getTopMeals(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        try {
            extractToken(token); // chỉ check, không dùng token thật thì để vậy
            Page<MealDTO> topMeals = statisticsService.getTopMeals(startDate, endDate, limit, offset);
            return responseHandler.responseSuccess("Lấy danh sách món ăn phổ biến thành công", topMeals);
        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return responseHandler.handleServerError("Đã xảy ra lỗi khi lấy danh sách món ăn phổ biến");
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getBookingStatistics(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String type, // "daily" or "monthly"
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            String authToken = extractToken(token);
            Page<Map<String, Object>> stats = statisticsService.getBookingStatistics(startDate, endDate, type, limit, offset);
            return responseHandler.responseSuccess("Lấy thống kê đặt bàn thành công", stats);
        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return responseHandler.handleServerError("Đã xảy ra lỗi khi lấy thống kê đặt bàn");
        }
    }

    @GetMapping("/bills")
    public ResponseEntity<?> getBillStatistics(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String type, // "daily" or "monthly"
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            String authToken = extractToken(token);
            Page<Map<String, Object>> stats = statisticsService.getBillStatistics(startDate, endDate, type, limit, offset);
            return responseHandler.responseSuccess("Lấy thống kê hóa đơn thành công", stats);
        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return responseHandler.handleServerError("Đã xảy ra lỗi khi lấy thống kê hóa đơn");
        }
    }

    @GetMapping("/meals")
    public ResponseEntity<?> getMealStatistics(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            String authToken = extractToken(token);
            Map<String, Object> stats = statisticsService.getMealStatistics(startDate, endDate);
            return responseHandler.responseSuccess("Lấy thống kê món ăn thành công", stats);
        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return responseHandler.handleServerError("Đã xảy ra lỗi khi lấy thống kê món ăn");
        }
    }

//    @GetMapping("/overview-details")
//    public ResponseEntity<?> getOverviewDetails(
//            @RequestHeader("Authorization") String token,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//        try {
//            String authToken = extractToken(token);
//            Map<String, Object> details = statisticsService.getOverviewDetails(startDate, endDate);
//            return responseHandler.responseSuccess("Lấy chi tiết thống kê tổng quan thành công", details);
//        } catch (ForbiddenException e) {
//            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
//        } catch (Exception e) {
//            return responseHandler.handleServerError("Đã xảy ra lỗi khi lấy chi tiết thống kê tổng quan");
//        }
//    }
@GetMapping("/overview-details")
public ResponseEntity<?> getOverviewDetails(
        @RequestHeader("Authorization") String token,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(defaultValue = "0") int offset) {

    try {
        extractToken(token); // chỉ check token có hay không

        Map<String, Object> details = statisticsService.getOverviewDetails(startDate, endDate, limit, offset);

        return responseHandler.responseSuccess("Lấy chi tiết thống kê tổng quan thành công", details);
    } catch (ForbiddenException e) {
        return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
    } catch (Exception e) {
        e.printStackTrace();
        return responseHandler.handleServerError("Đã xảy ra lỗi khi lấy chi tiết thống kê tổng quan");
    }
}
}
