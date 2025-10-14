package com.example.j2ee_project.service.category;

import com.example.j2ee_project.model.dto.CategoryDTO;
import com.example.j2ee_project.model.request.category.CategoryRequest;
import org.springframework.data.domain.Page;

public interface CategoryServiceInterface {
    CategoryDTO createCategory(CategoryRequest categoryRequest);

    Page<CategoryDTO> getAllCategories(int offset, int limit, String search);

    CategoryDTO getCategoryById(Integer categoryId);

    CategoryDTO updateCategory(Integer categoryId, CategoryRequest categoryRequest);

    void deleteCategory(Integer categoryId);
}