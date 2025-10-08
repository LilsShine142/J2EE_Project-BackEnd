package com.example.j2ee_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.j2ee_project.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

}
