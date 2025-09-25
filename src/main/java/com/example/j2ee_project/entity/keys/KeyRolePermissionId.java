package com.example.j2ee_project.entity.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyRolePermissionId implements Serializable {
    @Column(name = "roleid")
    private Integer roleID;

    @Column(name = "permissionid")
    private Integer permissionID;
}