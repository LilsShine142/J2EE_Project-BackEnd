package com.example.j2ee_project.entity;

import com.example.j2ee_project.entity.keys.KeyRolePermissionId;
import jakarta.persistence.*;
import lombok.*;

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

    public RolePermission(Role role, Permission permission) {
        this.id = new KeyRolePermissionId(role.getRoleID(), permission.getPermissionID());
        this.role = role;
        this.permission = permission;
    }

    public RolePermission() {
    }
}