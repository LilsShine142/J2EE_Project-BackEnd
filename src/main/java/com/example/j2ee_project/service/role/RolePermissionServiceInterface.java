package com.example.j2ee_project.service.role;

import com.example.j2ee_project.model.dto.RolePermissionDTO;
import com.example.j2ee_project.model.request.role.RolePermissionRequest;
import org.springframework.data.domain.Page;

public interface RolePermissionServiceInterface {
    RolePermissionDTO createRolePermission(RolePermissionRequest request);

    Page<RolePermissionDTO> getAllRolePermissions(int offset, int limit, String search);

    RolePermissionDTO getRolePermissionById(Integer roleId, Integer permissionId);

    RolePermissionDTO updateRolePermission(Integer roleId, Integer permissionId, RolePermissionRequest request);

    void deleteRolePermission(Integer roleId, Integer permissionId);
}