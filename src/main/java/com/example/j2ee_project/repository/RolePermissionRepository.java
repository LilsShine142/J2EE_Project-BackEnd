package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.RolePermission;
import com.example.j2ee_project.entity.keys.KeyRolePermissionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, KeyRolePermissionId> {
    Page<RolePermission> findByRoleRoleNameContainingIgnoreCaseOrPermissionPermissionNameContainingIgnoreCase(
            String roleName, String permissionName, Pageable pageable);
}