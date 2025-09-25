package com.example.j2ee_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permissionid")
    private Integer permissionID;

    @Column(name = "permissionname", nullable = false, unique = true, length = 50)
    private String permissionName;

    @Column(name = "description", length = 100)
    private String description;

    @OneToMany(mappedBy = "permission")
    private List<RolePermission> rolePermissions;
}