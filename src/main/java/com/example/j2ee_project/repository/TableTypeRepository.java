package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.TableType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableTypeRepository extends JpaRepository<TableType, Integer> {
    boolean existsByTypeName(String typeName);
}


