package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.RoleDTO;
import com.example.j2ee_project.model.request.role.RoleRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.role.RoleServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleServiceInterface roleService;
    private final ResponseHandler responseHandler;

    @Autowired
    public RoleController(RoleServiceInterface roleService, ResponseHandler responseHandler) {
        this.roleService = roleService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleRequest request) {
        RoleDTO response = roleService.createRole(request);
        return responseHandler.responseCreated("Tạo vai trò thành công", response);
    }

    @GetMapping
    public ResponseEntity<?> getAllRoles(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<RoleDTO> page = roleService.getAllRoles(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách vai trò thành công", page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Integer id) {
        RoleDTO response = roleService.getRoleById(id);
        return responseHandler.responseSuccess("Lấy thông tin vai trò thành công", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Integer id, @Valid @RequestBody RoleRequest request) {
        RoleDTO response = roleService.updateRole(id, request);
        return responseHandler.responseSuccess("Cập nhật vai trò thành công", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return responseHandler.responseSuccess("Xóa vai trò thành công", null);
    }
}