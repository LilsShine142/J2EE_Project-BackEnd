package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

        Page<Role> findByRoleNameContainingIgnoreCase(String roleName, Pageable pageable);
}