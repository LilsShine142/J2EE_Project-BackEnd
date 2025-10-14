package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.RolePermissionDTO;
import com.example.j2ee_project.model.request.role.RolePermissionRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.role.RolePermissionServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rolepermissions")
public class RolePermissionController {

    private final RolePermissionServiceInterface rolePermissionService;
    private final ResponseHandler responseHandler;

    @Autowired
    public RolePermissionController(RolePermissionServiceInterface rolePermissionService, ResponseHandler responseHandler) {
        this.rolePermissionService = rolePermissionService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRolePermission(@Valid @RequestBody RolePermissionRequest request) {
        RolePermissionDTO response = rolePermissionService.createRolePermission(request);
        return responseHandler.responseCreated("Cấp quyền cho vai trò thành công", response);
    }

    @GetMapping
    public ResponseEntity<?> getAllRolePermissions(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<RolePermissionDTO> page = rolePermissionService.getAllRolePermissions(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách liên kết vai trò-quyền thành công", page);
    }

    @GetMapping("/{roleId}/{permissionId}")
    public ResponseEntity<?> getRolePermissionById(@PathVariable Integer roleId, @PathVariable Integer permissionId) {
        RolePermissionDTO response = rolePermissionService.getRolePermissionById(roleId, permissionId);
        return responseHandler.responseSuccess("Lấy thông tin liên kết vai trò-quyền thành công", response);
    }

    @PutMapping("/{roleId}/{permissionId}")
    public ResponseEntity<?> updateRolePermission(@PathVariable Integer roleId, @PathVariable Integer permissionId,
                                                  @Valid @RequestBody RolePermissionRequest request) {
        RolePermissionDTO response = rolePermissionService.updateRolePermission(roleId, permissionId, request);
        return responseHandler.responseSuccess("Cập nhật liên kết vai trò-quyền thành công", response);
    }

    @DeleteMapping("/{roleId}/{permissionId}")
    public ResponseEntity<?> deleteRolePermission(@PathVariable Integer roleId, @PathVariable Integer permissionId) {
        rolePermissionService.deleteRolePermission(roleId, permissionId);
        return responseHandler.responseSuccess("Xóa liên kết vai trò-quyền thành công", null);
    }
}