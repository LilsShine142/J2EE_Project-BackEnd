package com.example.j2ee_project.service.role;

import com.example.j2ee_project.model.dto.RoleDTO;
import com.example.j2ee_project.model.request.role.RoleRequest;
import org.springframework.data.domain.Page;

public interface RoleServiceInterface {

    RoleDTO createRole(RoleRequest request);

    Page<RoleDTO> getAllRoles(int offset, int limit, String search);

    RoleDTO getRoleById(Integer roleId);

    RoleDTO updateRole(Integer roleId, RoleRequest request);

    void deleteRole(Integer roleId);

    String getRoleNameByRoleId(Integer roleId);
}