package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Status;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Integer> {
    Optional<Status> findByStatusName(String statusName);
}