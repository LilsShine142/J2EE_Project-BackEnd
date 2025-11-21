package com.example.j2ee_project.service.category;

import com.example.j2ee_project.entity.Category;
import com.example.j2ee_project.exception.ForbiddenException;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.CategoryDTO;
import com.example.j2ee_project.model.request.category.CategoryRequest;
import com.example.j2ee_project.repository.CategoryRepository;
import com.example.j2ee_project.utils._enum.EPermission;
import com.example.j2ee_project.utils.role_permission.RolePermissionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryServiceInterface {

    private final CategoryRepository categoryRepository;
    private final RolePermissionUtils rolePermissionUtils;

    @Override
    @Transactional
    public CategoryDTO createCategory(String token, CategoryRequest categoryRequest) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để tạo danh mục!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.CREATE_CATEGORY.getCode())) {
            throw new ForbiddenException("Bạn không có quyền tạo danh mục!");
        }

        Category category = new Category();
        category.setCategoryName(categoryRequest.getCategoryName());
        category.setDescription(categoryRequest.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return mapToCategoryDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategories(String token, int offset, int limit, String search) {
        if (token != null && !rolePermissionUtils.hasPermission(token, EPermission.VIEW_CATEGORY.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem danh sách danh mục!");
        }

        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Category> categoryPage = categoryRepository.findByCategoryNameContainingIgnoreCase(search, pageable);
        return categoryPage.map(this::mapToCategoryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(String token, Integer categoryId) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để xem thông tin danh mục!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.VIEW_CATEGORY.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem thông tin danh mục!");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));
        return mapToCategoryDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(String token, Integer categoryId, CategoryRequest categoryRequest) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để cập nhật danh mục!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.UPDATE_CATEGORY.getCode())) {
            throw new ForbiddenException("Bạn không có quyền cập nhật danh mục!");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));

        category.setCategoryName(categoryRequest.getCategoryName());
        category.setDescription(categoryRequest.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return mapToCategoryDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(String token, Integer categoryId) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để xóa danh mục!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.DELETE_CATEGORY.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xóa danh mục!");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));
        categoryRepository.delete(category);
    }

    private CategoryDTO mapToCategoryDTO(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryID(category.getCategoryID());
        categoryDTO.setCategoryName(category.getCategoryName());
        categoryDTO.setDescription(category.getDescription());
        return categoryDTO;
    }
}