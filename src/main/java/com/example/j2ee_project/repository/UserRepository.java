package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRoleId(Integer roleId);

    @Query("SELECT u FROM User u JOIN Role r ON u.roleId = r.roleID WHERE r.roleName = :roleName")
    List<User> findByRoleRoleName(@Param("roleName") String roleName);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u " +
            "WHERE (:username IS NULL OR u.username LIKE %:username%) " +
            "AND (:email IS NULL OR u.email LIKE %:email%) " +
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:roleId IS NULL OR u.roleId = :roleId)")
    Page<User> findUsersFiltered(
            @Param("username") String username,
            @Param("email") String email,
            @Param("status") String status,
            @Param("roleId") Integer roleId,
            Pageable pageable);
}