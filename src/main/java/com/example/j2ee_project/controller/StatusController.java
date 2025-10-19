package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.StatusDTO;
import com.example.j2ee_project.model.request.status.StatusRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.status.StatusServiceInterface;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statuses")
@Tag(name = "Status Management", description = "APIs for managing system or entity statuses")
public class StatusController {

    private final StatusServiceInterface statusService;
    private final ResponseHandler responseHandler;

    @Autowired
    public StatusController(StatusServiceInterface statusService, ResponseHandler responseHandler) {
        this.statusService = statusService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createStatus(@Valid @RequestBody StatusRequest request) {
        StatusDTO response = statusService.createStatus(request);
        return responseHandler.responseCreated("Tạo trạng thái thành công", response);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllStatuses(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<StatusDTO> page = statusService.getAllStatuses(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách trạng thái thành công", page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStatusById(@PathVariable Integer id) {
        StatusDTO response = statusService.getStatusById(id);
        return responseHandler.responseSuccess("Lấy thông tin trạng thái thành công", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @Valid @RequestBody StatusRequest request) {
        StatusDTO response = statusService.updateStatus(id, request);
        return responseHandler.responseSuccess("Cập nhật trạng thái thành công", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStatus(@PathVariable Integer id) {
        statusService.deleteStatus(id);
        return responseHandler.responseSuccess("Xóa trạng thái thành công", null);
    }
}