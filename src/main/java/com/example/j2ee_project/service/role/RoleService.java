package com.example.j2ee_project.service.role;

import com.example.j2ee_project.entity.Role;
import com.example.j2ee_project.repository.RoleRepository;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.exception.DuplicateResourceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Lấy role theo ID, ném exception nếu không tồn tại
    public Role findById(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với ID: " + roleId));
    }

    // Tìm role theo tên
    public Role findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với tên: " + roleName));
    }

    // Lấy roleName theo roleId
    public String getRoleNameByRoleId(Integer roleId) {
        return findById(roleId).getRoleName();
    }

    // Lấy toàn bộ role (sắp xếp theo tên)
    public List<Role> getAllRoles() {
        return roleRepository.findAllByOrderByRoleName();
    }

    // Tạo role mới
    public Role createRole(Role role) {
        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new DuplicateResourceException("Role với tên '" + role.getRoleName() + "' đã tồn tại");
        }

        LocalDateTime now = LocalDateTime.now();
        role.setCreatedAt(now);
        role.setUpdatedAt(now);

        return roleRepository.save(role);
    }

    // Cập nhật role
    public Role updateRole(Integer roleId, Role role) {
        Role existingRole = findById(roleId);

        if (role.getRoleName() != null && !role.getRoleName().equals(existingRole.getRoleName())) {
            if (roleRepository.existsByRoleName(role.getRoleName())) {
                throw new DuplicateResourceException("Role với tên '" + role.getRoleName() + "' đã tồn tại");
            }
            existingRole.setRoleName(role.getRoleName());
        }

        if (role.getRoleDescription() != null) {
            existingRole.setRoleDescription(role.getRoleDescription());
        }

        existingRole.setUpdatedAt(LocalDateTime.now());

        return roleRepository.save(existingRole);
    }

    // Xóa role
    // public void deleteRole(Integer roleId) {
    // if (!roleRepository.existsById(roleId)) {
    // throw new ResourceNotFoundException("Không tìm thấy role với ID: " + roleId);
    // }

    // Long userCount = roleRepository.countUsersWithRole(roleId);
    // if (userCount > 0) {
    // throw new IllegalStateException("Không thể xóa role vì vẫn còn " + userCount
    // + " user đang sử dụng");
    // }

    // roleRepository.deleteById(roleId);
    // }

    // Kiểm tra role tồn tại
    public boolean existsById(Integer roleId) {
        return roleRepository.existsById(roleId);
    }

    // // Lấy role mặc định (USER), nếu chưa có thì tạo
    // public Role getDefaultRole() {
    // return roleRepository.findByIsDefaultTrue()
    // .orElseGet(() -> {
    // Role defaultRole = Role.builder()
    // .roleName("USER")
    // .roleDescription("Default user role")
    // // .isDefault(true)
    // .createdAt(LocalDateTime.now())
    // .updatedAt(LocalDateTime.now())
    // .build();
    // return roleRepository.save(defaultRole);
    // });
    // }

    // Khởi tạo role mặc định ban đầu
    public void initializeDefaultRoles() {
        try {
            if (!roleRepository.existsByRoleName("USER")) {
                createRole(Role.builder()
                        .roleName("USER")
                        .roleDescription("Standard user role")
                        // .isDefault(true)
                        .build());
                System.out.println("✅ Created default USER role");
            }

            if (!roleRepository.existsByRoleName("ADMIN")) {
                createRole(Role.builder()
                        .roleName("ADMIN")
                        .roleDescription("Administrator role")
                        // .isDefault(false)
                        .build());
                System.out.println("✅ Created ADMIN role");
            }
        } catch (Exception e) {
            System.err.println("❌ Error initializing default roles: " + e.getMessage());
        }
    }
}
