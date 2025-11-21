package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {
    Page<Log> findByTableNameContainingIgnoreCaseOrActionContainingIgnoreCase(
            String tableName, String action, Pageable pageable);

    @Query("SELECT l FROM Log l WHERE l.tableName = 'emails' " +
           "AND (:userId IS NULL OR l.user.userID = :userId) " +
           "AND (:startDate IS NULL OR l.changeTime >= :startDate) " +
           "AND (:endDate IS NULL OR l.changeTime <= :endDate) " +
           "AND (:type IS NULL OR l.action = :type)")
    Page<Log> findEmailHistory(@Param("userId") Integer userId,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate,
                               @Param("type") String type,
                               Pageable pageable);
}