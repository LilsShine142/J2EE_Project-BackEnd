package com.example.j2ee_project.entity;

import com.example.j2ee_project.entity.keys.KeyRolePermissionId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rolepermissions")
public class RolePermission {
    @EmbeddedId
    private KeyRolePermissionId id;

    @ManyToOne
    @MapsId("roleID")
    @JoinColumn(name = "roleid")
    private Role role;

    @ManyToOne
    @MapsId("permissionID")
    @JoinColumn(name = "permissionid")
    private Permission permission;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    public RolePermission(Role role, Permission permission) {
        this.id = new KeyRolePermissionId(role.getRoleID(), permission.getPermissionID());
        this.role = role;
        this.permission = permission;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RolePermission() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}