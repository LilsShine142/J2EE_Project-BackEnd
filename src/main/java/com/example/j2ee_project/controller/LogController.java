package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.LogDTO;
import com.example.j2ee_project.model.request.log.LogRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.log.LogServiceInterface;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log Management", description = "APIs for managing system logs and audit trails")
public class LogController {

    private final LogServiceInterface logService;
    private final ResponseHandler responseHandler;

    @Autowired
    public LogController(LogServiceInterface logService, ResponseHandler responseHandler) {
        this.logService = logService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLog(@Valid @RequestBody LogRequest request) {
        LogDTO response = logService.createLog(request);
        return responseHandler.responseCreated("Tạo log thành công", response);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllLogs(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<LogDTO> page = logService.getAllLogs(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách log thành công", page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLogById(@PathVariable Integer id) {
        LogDTO response = logService.getLogById(id);
        return responseHandler.responseSuccess("Lấy thông tin log thành công", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLog(@PathVariable Integer id, @Valid @RequestBody LogRequest request) {
        LogDTO response = logService.updateLog(id, request);
        return responseHandler.responseSuccess("Cập nhật log thành công", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLog(@PathVariable Integer id) {
        logService.deleteLog(id);
        return responseHandler.responseSuccess("Xóa log thành công", null);
    }
}