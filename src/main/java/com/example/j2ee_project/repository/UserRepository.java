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

    @Query("SELECT u FROM User u WHERE u.role.roleID = :roleId")
    List<User> findByRoleId(@Param("roleId") Integer roleId);

    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName")
    List<User> findByRoleRoleName(@Param("roleName") String roleName);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u " +
            "WHERE (:username IS NULL OR u.username LIKE %:username%) " +
            "AND (:search IS NULL OR u.email LIKE %:search% OR u.fullName LIKE %:search%) " +
            "AND (:statusId IS NULL OR u.status.statusID = :statusId) " +
            "AND (:roleId IS NULL OR u.role.roleID = :roleId)")
    Page<User> findUsersFiltered(
            @Param("username") String username,
            @Param("search") String search,
            @Param("statusId") Integer statusId,
            @Param("roleId") Integer roleId,
            Pageable pageable);
}