package com.example.j2ee_project.service.category;

import com.example.j2ee_project.model.dto.CategoryDTO;
import com.example.j2ee_project.model.request.category.CategoryRequest;
import org.springframework.data.domain.Page;

public interface CategoryServiceInterface {
    CategoryDTO createCategory(String token, CategoryRequest categoryRequest);

    Page<CategoryDTO> getAllCategories(String token, int offset, int limit, String search);

    CategoryDTO getCategoryById(String token, Integer categoryId);

    CategoryDTO updateCategory(String token, Integer categoryId, CategoryRequest categoryRequest);

    void deleteCategory(String token, Integer categoryId);
}