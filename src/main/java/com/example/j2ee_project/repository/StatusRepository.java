package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    Page<Status> findByStatusNameContainingIgnoreCase(String statusName, Pageable pageable);

    Optional<Status> findByStatusName(String statusName);
}