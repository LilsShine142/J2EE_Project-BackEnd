package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.RestaurantTableDTO;
import com.example.j2ee_project.model.request.table.TableRequestDTO;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.table.TableService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tables")
@Tag(name = "Table Management", description = "APIs for managing restaurant tables")
@RequiredArgsConstructor
public class TableController {
    private final TableService tableService;
    private final ResponseHandler responseHandler;

    @PostMapping("/create")
    public ResponseEntity<?> createTable(@Valid @RequestBody TableRequestDTO tableRequestDTO) {
        RestaurantTableDTO response = tableService.createTable(tableRequestDTO);
        return responseHandler.responseCreated("Tạo bàn thành công", response);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllTables(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer capacity) {
        Page<RestaurantTableDTO> tablePage = tableService.getAllTables(offset, limit, search, statusId, capacity);
        return responseHandler.responseSuccess("Lấy danh sách bàn thành công", tablePage);
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableTables(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bookingDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Integer capacity) {
        Page<RestaurantTableDTO> tablePage = tableService.getAvailableTables(offset, limit, bookingDate, startTime, endTime, capacity);
        return responseHandler.responseSuccess("Lấy danh sách bàn khả dụng thành công", tablePage);
    }

    @GetMapping("/{tableId}")
    public ResponseEntity<?> getTableById(@PathVariable Integer tableId) {
        RestaurantTableDTO response = tableService.getTableById(tableId);
        return responseHandler.responseSuccess("Lấy thông tin bàn thành công", response);
    }

    @PutMapping("/{tableId}")
    public ResponseEntity<?> updateTable(@PathVariable Integer tableId, @Valid @RequestBody TableRequestDTO tableUpdateRequestDTO) {
        RestaurantTableDTO response = tableService.updateTable(tableId, tableUpdateRequestDTO);
        return responseHandler.responseSuccess("Cập nhật bàn thành công", response);
    }

    @DeleteMapping("/{tableId}")
    public ResponseEntity<?> deleteTable(@PathVariable Integer tableId) {
        tableService.deleteTable(tableId);
        return responseHandler.responseSuccess("Xóa bàn thành công", null);
    }
}