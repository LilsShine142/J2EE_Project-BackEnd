package com.example.j2ee_project.service.permission;

import com.example.j2ee_project.model.dto.PermissionDTO;
import com.example.j2ee_project.model.request.permission.PermissionRequest;
import org.springframework.data.domain.Page;

public interface PermissionServiceInterface {
    PermissionDTO createPermission(PermissionRequest request);

    Page<PermissionDTO> getAllPermissions(int offset, int limit, String search);

    PermissionDTO getPermissionById(Integer permissionId);

    PermissionDTO updatePermission(Integer permissionId, PermissionRequest request);

    void deletePermission(Integer permissionId);
}