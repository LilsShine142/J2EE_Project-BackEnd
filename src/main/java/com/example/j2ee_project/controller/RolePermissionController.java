package com.example.j2ee_project.controller;

import com.example.j2ee_project.exception.ForbiddenException;
import com.example.j2ee_project.model.dto.RolePermissionDTO;
import com.example.j2ee_project.model.request.role.RolePermissionRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.role.RolePermissionServiceInterface;
import com.example.j2ee_project.utils._enum.EPermission;
import com.example.j2ee_project.utils.role_permission.RolePermissionUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rolepermissions")
@Tag(name = "Role Permission Management", description = "APIs for managing role-permission associations")
public class RolePermissionController {

    private final RolePermissionServiceInterface rolePermissionService;
    private final ResponseHandler responseHandler;
    private final RolePermissionUtils rolePermissionUtils;

    @Autowired
    public RolePermissionController(RolePermissionServiceInterface rolePermissionService, ResponseHandler responseHandler,
                                   RolePermissionUtils rolePermissionUtils) {
        this.rolePermissionService = rolePermissionService;
        this.responseHandler = responseHandler;
        this.rolePermissionUtils = rolePermissionUtils;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRolePermission(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody RolePermissionRequest request) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.CREATE_ROLE_PERMISSION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền cấp quyền cho vai trò!");
        }
        RolePermissionDTO response = rolePermissionService.createRolePermission(request);
        return responseHandler.responseCreated("Cấp quyền cho vai trò thành công", response);
    }

    @GetMapping
    public ResponseEntity<?> getAllRolePermissions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.VIEW_ROLE_PERMISSION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem danh sách liên kết vai trò-quyền!");
        }
        Page<RolePermissionDTO> page = rolePermissionService.getAllRolePermissions(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách liên kết vai trò-quyền thành công", page);
    }

    @GetMapping("/{roleId}/{permissionId}")
    public ResponseEntity<?> getRolePermissionById(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer roleId,
            @PathVariable Integer permissionId) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.VIEW_ROLE_PERMISSION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem thông tin liên kết vai trò-quyền!");
        }
        RolePermissionDTO response = rolePermissionService.getRolePermissionById(roleId, permissionId);
        return responseHandler.responseSuccess("Lấy thông tin liên kết vai trò-quyền thành công", response);
    }

    @PutMapping("/{roleId}/{permissionId}")
    public ResponseEntity<?> updateRolePermission(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer roleId,
            @PathVariable Integer permissionId,
            @Valid @RequestBody RolePermissionRequest request) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.UPDATE_ROLE_PERMISSION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền cập nhật liên kết vai trò-quyền!");
        }
        RolePermissionDTO response = rolePermissionService.updateRolePermission(roleId, permissionId, request);
        return responseHandler.responseSuccess("Cập nhật liên kết vai trò-quyền thành công", response);
    }

    @DeleteMapping("/{roleId}/{permissionId}")
    public ResponseEntity<?> deleteRolePermission(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer roleId,
            @PathVariable Integer permissionId) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.DELETE_ROLE_PERMISSION.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xóa liên kết vai trò-quyền!");
        }
        rolePermissionService.deleteRolePermission(roleId, permissionId);
        return responseHandler.responseSuccess("Xóa liên kết vai trò-quyền thành công", null);
    }
}