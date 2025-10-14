package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Page<Permission> findByPermissionNameContainingIgnoreCase(String permissionName, Pageable pageable);
}