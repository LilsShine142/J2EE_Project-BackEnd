package com.example.j2ee_project.service.permission;

import com.example.j2ee_project.entity.*;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.LogDTO;
import com.example.j2ee_project.model.dto.NotificationDTO;
import com.example.j2ee_project.model.dto.PermissionDTO;
import com.example.j2ee_project.model.request.log.LogRequest;
import com.example.j2ee_project.model.request.notification.NotificationRequest;
import com.example.j2ee_project.model.request.permission.PermissionRequest;
import com.example.j2ee_project.repository.PermissionRepository;
import com.example.j2ee_project.repository.RolePermissionRepository;
import com.example.j2ee_project.repository.RoleRepository;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.service.log.LogServiceInterface;
import com.example.j2ee_project.service.notification.NotificationServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService implements PermissionServiceInterface {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final LogServiceInterface logService;
    private final NotificationServiceInterface notificationService;

    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionRequest request) {
        Permission permission = new Permission();
        permission.setPermissionName(request.getPermissionName());
        permission.setDescription(request.getDescription());

        Permission saved = permissionRepository.save(permission);

        // Ghi log
        logService.createLog(new LogRequest());

        logService.createLog(new LogRequest("permissions", saved.getPermissionID(), "CREATE",
                "Created permission: " + saved.getPermissionName(), null));

        // Gửi thông báo cho admin (giả sử admin có userID = 1)
        notificationService.createNotification(new NotificationRequest(1, "New Permission",
                "Permission " + saved.getPermissionName() + " created", "No", "NONE", null));

        return mapToPermissionDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionDTO> getAllPermissions(int offset, int limit, String search) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Permission> page = permissionRepository.findByPermissionNameContainingIgnoreCase(search, pageable);
        return page.map(this::mapToPermissionDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDTO getPermissionById(Integer permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quyền với ID: " + permissionId));
        return mapToPermissionDTO(permission);
    }

    @Override
    @Transactional
    public PermissionDTO updatePermission(Integer permissionId, PermissionRequest request) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quyền với ID: " + permissionId));

        permission.setPermissionName(request.getPermissionName());
        permission.setDescription(request.getDescription());

        Permission updated = permissionRepository.save(permission);

        // Ghi log
        logService.createLog(new LogRequest("permissions", updated.getPermissionID(), "UPDATE",
                "Updated permission: " + updated.getPermissionName(), null));

        // Gửi thông báo
        notificationService.createNotification(new NotificationRequest(1, "Permission Updated",
                "Permission " + updated.getPermissionName() + " updated", "No", "NONE", null));

        return mapToPermissionDTO(updated);
    }

    @Override
    @Transactional
    public void deletePermission(Integer permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quyền với ID: " + permissionId));

        permissionRepository.delete(permission);

        // Ghi log
        logService.createLog(new LogRequest("permissions", permissionId, "DELETE",
                "Deleted permission: " + permission.getPermissionName(), null));

        // Gửi thông báo
        notificationService.createNotification(new NotificationRequest(1, "Permission Deleted",
                "Permission " + permission.getPermissionName() + " deleted", "No", "NONE", null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissionsByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Integer roleId = user.getRole().getRoleID();

        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole_RoleID(roleId);

        return rolePermissions.stream()
                .map(rolePermission -> mapToPermissionDTO(rolePermission.getPermission()))
                .collect(Collectors.toList());
    }

    private PermissionDTO mapToPermissionDTO(Permission permission) {
        PermissionDTO dto = new PermissionDTO();
        dto.setPermissionID(permission.getPermissionID());
        dto.setPermissionName(permission.getPermissionName());
        dto.setDescription(permission.getDescription());
        return dto;
    }
}