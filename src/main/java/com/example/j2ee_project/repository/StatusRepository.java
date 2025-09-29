package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    Status findByStatusName(String statusName);
}