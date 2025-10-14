package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.TableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableTypeRepository extends JpaRepository<TableType, Integer> {
    Page<TableType> findByTypeNameContainingIgnoreCase(String typeName, Pageable pageable);
}