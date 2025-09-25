package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {

    boolean existsByRole_RoleIDAndPermission_PermissionID(Integer roleID, Integer permissionID);

    Optional<RolePermission> findByRole_RoleIDAndPermission_PermissionID(Integer roleID, Integer permissionID);

    List<RolePermission> findByRole_RoleID(Integer roleID);

    List<RolePermission> findByPermission_PermissionID(Integer permissionID);;

}