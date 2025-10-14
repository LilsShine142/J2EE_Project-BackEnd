package com.example.j2ee_project.service.role;

import com.example.j2ee_project.entity.Permission;
import com.example.j2ee_project.entity.Role;
import com.example.j2ee_project.entity.RolePermission;
import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.entity.keys.KeyRolePermissionId;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.RolePermissionDTO;
import com.example.j2ee_project.model.request.log.LogRequest;
import com.example.j2ee_project.model.request.notification.NotificationRequest;
import com.example.j2ee_project.model.request.role.RolePermissionRequest;
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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RolePermissionService implements RolePermissionServiceInterface {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final LogServiceInterface logService;
    private final NotificationServiceInterface notificationService;

    @Override
    @Transactional
    public RolePermissionDTO createRolePermission(RolePermissionRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + request.getRoleId()));
        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quyền với ID: " + request.getPermissionId()));
        User grantedBy = userRepository.findById(request.getGrantedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getGrantedByUserId()));

        // Kiểm tra quyền của grantedBy (giả sử user cần có quyền "MANAGE_PERMISSIONS")
        // Đây là logic nghiệp vụ, cần tích hợp Spring Security thực tế
        // Ví dụ: checkPermission(grantedBy, "MANAGE_PERMISSIONS");

        RolePermission rolePermission = new RolePermission(role, permission);
        RolePermission saved = rolePermissionRepository.save(rolePermission);

        // Ghi log
        logService.createLog(new LogRequest("rolepermissions", saved.getId().hashCode(), "CREATE",
                "Granted permission " + permission.getPermissionName() + " to role " + role.getRoleName(),
                request.getGrantedByUserId()));

        // Gửi thông báo
        notificationService.createNotification(new NotificationRequest(request.getGrantedByUserId(),
                "Permission Granted",
                "Permission " + permission.getPermissionName() + " granted to role " + role.getRoleName(),
                "No"));

        return mapToRolePermissionDTO(saved, request.getGrantedByUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RolePermissionDTO> getAllRolePermissions(int offset, int limit, String search) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<RolePermission> page = rolePermissionRepository.findByRoleRoleNameContainingIgnoreCaseOrPermissionPermissionNameContainingIgnoreCase(
                search, search, pageable);
        return page.map(rp -> mapToRolePermissionDTO(rp, null));
    }

    @Override
    @Transactional(readOnly = true)
    public RolePermissionDTO getRolePermissionById(Integer roleId, Integer permissionId) {
        KeyRolePermissionId id = new KeyRolePermissionId(roleId, permissionId);
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên kết vai trò-quyền với roleID: " + roleId + " và permissionID: " + permissionId));
        return mapToRolePermissionDTO(rolePermission, null);
    }

    @Override
    @Transactional
    public RolePermissionDTO updateRolePermission(Integer roleId, Integer permissionId, RolePermissionRequest request) {
        KeyRolePermissionId id = new KeyRolePermissionId(roleId, permissionId);
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên kết vai trò-quyền với roleID: " + roleId + " và permissionID: " + permissionId));

        Role newRole = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + request.getRoleId()));
        Permission newPermission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quyền với ID: " + request.getPermissionId()));
        User grantedBy = userRepository.findById(request.getGrantedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getGrantedByUserId()));

        // Kiểm tra quyền của grantedBy
        // checkPermission(grantedBy, "MANAGE_PERMISSIONS");

        rolePermission.setRole(newRole);
        rolePermission.setPermission(newPermission);

        RolePermission updated = rolePermissionRepository.save(rolePermission);

        // Ghi log
        logService.createLog(new LogRequest("rolepermissions", updated.getId().hashCode(), "UPDATE",
                "Updated permission " + newPermission.getPermissionName() + " for role " + newRole.getRoleName(),
                request.getGrantedByUserId()));

        // Gửi thông báo
        notificationService.createNotification(new NotificationRequest(request.getGrantedByUserId(),
                "Permission Updated",
                "Permission " + newPermission.getPermissionName() + " updated for role " + newRole.getRoleName(),
                "No"));

        return mapToRolePermissionDTO(updated, request.getGrantedByUserId());
    }

    @Override
    @Transactional
    public void deleteRolePermission(Integer roleId, Integer permissionId) {
        KeyRolePermissionId id = new KeyRolePermissionId(roleId, permissionId);
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên kết vai trò-quyền với roleID: " + roleId + " và permissionID: " + permissionId));

        rolePermissionRepository.delete(rolePermission);

        // Ghi log
        logService.createLog(new LogRequest("rolepermissions", id.hashCode(), "DELETE",
                "Removed permission " + rolePermission.getPermission().getPermissionName() + " from role " + rolePermission.getRole().getRoleName(),
                null));

        // Gửi thông báo
        notificationService.createNotification(new NotificationRequest(1, "Permission Removed",
                "Permission " + rolePermission.getPermission().getPermissionName() + " removed from role " + rolePermission.getRole().getRoleName(),
                "No"));
    }

    private RolePermissionDTO mapToRolePermissionDTO(RolePermission rolePermission, Integer grantedByUserId) {
        RolePermissionDTO dto = RolePermissionDTO.builder()
                .rolePermissionId(rolePermission.getId().hashCode())
                .roleId(rolePermission.getRole().getRoleID())
                .permissionId(rolePermission.getPermission().getPermissionID())
                .grantedAt(LocalDateTime.now())
                .grantedByUserId(grantedByUserId)
                .build();
        return dto;
    }
}