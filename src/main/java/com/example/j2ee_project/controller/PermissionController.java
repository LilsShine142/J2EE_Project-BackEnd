package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.PermissionDTO;
import com.example.j2ee_project.model.request.permission.PermissionRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.permission.PermissionServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionServiceInterface permissionService;
    private final ResponseHandler responseHandler;

    @Autowired
    public PermissionController(PermissionServiceInterface permissionService, ResponseHandler responseHandler) {
        this.permissionService = permissionService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPermission(@Valid @RequestBody PermissionRequest request) {
        PermissionDTO response = permissionService.createPermission(request);
        return responseHandler.responseCreated("Tạo quyền thành công", response);
    }

    @GetMapping
    public ResponseEntity<?> getAllPermissions(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<PermissionDTO> page = permissionService.getAllPermissions(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách quyền thành công", page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPermissionById(@PathVariable Integer id) {
        PermissionDTO response = permissionService.getPermissionById(id);
        return responseHandler.responseSuccess("Lấy thông tin quyền thành công", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePermission(@PathVariable Integer id, @Valid @RequestBody PermissionRequest request) {
        PermissionDTO response = permissionService.updatePermission(id, request);
        return responseHandler.responseSuccess("Cập nhật quyền thành công", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePermission(@PathVariable Integer id) {
        permissionService.deletePermission(id);
        return responseHandler.responseSuccess("Xóa quyền thành công", null);
    }
}