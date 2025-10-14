package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {
    Page<Log> findByTableNameContainingIgnoreCaseOrActionContainingIgnoreCase(
            String tableName, String action, Pageable pageable);
}