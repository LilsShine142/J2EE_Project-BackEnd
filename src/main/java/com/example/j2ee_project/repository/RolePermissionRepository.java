package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.RolePermission;
import com.example.j2ee_project.entity.keys.KeyRolePermissionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, KeyRolePermissionId> {

    @Query("SELECT rp FROM RolePermission rp " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "LOWER(rp.role.roleName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(rp.permission.permissionName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RolePermission> findByFilters(@Param("search") String search, Pageable pageable);
}