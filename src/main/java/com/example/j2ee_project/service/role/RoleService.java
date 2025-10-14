package com.example.j2ee_project.service.role;

import com.example.j2ee_project.entity.Role;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.RoleDTO;
import com.example.j2ee_project.model.request.log.LogRequest;
import com.example.j2ee_project.model.request.notification.NotificationRequest;
import com.example.j2ee_project.model.request.role.RoleRequest;
import com.example.j2ee_project.repository.RoleRepository;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.service.log.LogServiceInterface;
import com.example.j2ee_project.service.notification.NotificationServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoleService implements RoleServiceInterface {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LogServiceInterface logService;
    private final NotificationServiceInterface notificationService;

    @Override
    @Transactional
    public RoleDTO createRole(RoleRequest request) {
        Integer currentUserId = getCurrentUserId();

        Role role = Role.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Role saved = roleRepository.save(role);

        // Ghi log
        logService.createLog(new LogRequest(
                "roles",
                saved.getRoleID(),
                "CREATE",
                "Created role: " + saved.getRoleName(),
                currentUserId
        ));

        // Gửi thông báo
        notificationService.createNotification(new NotificationRequest(
                1,
                "New Role",
                "Role " + saved.getRoleName() + " created by user ID " + currentUserId,
                "No"
        ));

        return mapToRoleDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO> getAllRoles(int offset, int limit, String search) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Role> page = roleRepository.findByRoleNameContainingIgnoreCase(search, pageable);
        return page.map(this::mapToRoleDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Integer roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + roleId));
        return mapToRoleDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO updateRole(Integer roleId, RoleRequest request) {
        Integer currentUserId = getCurrentUserId();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + roleId));

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setUpdatedAt(LocalDateTime.now());

        Role updated = roleRepository.save(role);

        // Ghi log
        logService.createLog(new LogRequest(
                "roles",
                updated.getRoleID(),
                "UPDATE",
                "Updated role: " + updated.getRoleName(),
                currentUserId
        ));

        // Gửi thông báo
        notificationService.createNotification(new NotificationRequest(
                1,
                "Role Updated",
                "Role " + updated.getRoleName() + " updated by user ID " + currentUserId,
                "No"
        ));

        return mapToRoleDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRole(Integer roleId) {
        Integer currentUserId = getCurrentUserId();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + roleId));

        roleRepository.delete(role);

        // Ghi log
        logService.createLog(new LogRequest(
                "roles",
                roleId,
                "DELETE",
                "Deleted role: " + role.getRoleName(),
                currentUserId
        ));

        // Gửi thông báo
        notificationService.createNotification(new NotificationRequest(
                1,
                "Role Deleted",
                "Role " + role.getRoleName() + " deleted by user ID " + currentUserId,
                "No"
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public String getRoleNameByRoleId(Integer roleId) {
        Integer currentUserId = getCurrentUserId();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + roleId));

        // Ghi log
        logService.createLog(new LogRequest(
                "roles",
                roleId,
                "READ",
                "Retrieved role name: " + role.getRoleName(),
                currentUserId
        ));

        // Gửi thông báo (tùy chọn)
        notificationService.createNotification(new NotificationRequest(
                1,
                "Role Name Retrieved",
                "Role name " + role.getRoleName() + " retrieved by user ID " + currentUserId,
                "No"
        ));

        return role.getRoleName();
    }

    private RoleDTO mapToRoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setRoleID(role.getRoleID());
        dto.setRoleName(role.getRoleName());
        dto.setDescription(role.getDescription());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }

    private Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .map(user -> user.getUserID())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));
        }
        return userRepository.findById(1)
                .map(user -> user.getUserID())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy admin mặc định"));
    }
}