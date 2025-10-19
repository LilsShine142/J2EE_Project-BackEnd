package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.CategoryDTO;
import com.example.j2ee_project.model.request.category.CategoryRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.category.CategoryServiceInterface;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category Management", description = "APIs for managing restaurant categories")
public class CategoryController {

    private final CategoryServiceInterface categoryService;
    private final ResponseHandler responseHandler;

    @Autowired
    public CategoryController(CategoryServiceInterface categoryService, ResponseHandler responseHandler) {
        this.categoryService = categoryService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryDTO response = categoryService.createCategory(request);
        return responseHandler.responseCreated("Tạo danh mục thành công", response);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<CategoryDTO> categoryPage = categoryService.getAllCategories(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách danh mục thành công", categoryPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) {
        CategoryDTO response = categoryService.getCategoryById(id);
        return responseHandler.responseSuccess("Lấy thông tin danh mục thành công", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryRequest request) {
        CategoryDTO response = categoryService.updateCategory(id, request);
        return responseHandler.responseSuccess("Cập nhật danh mục thành công", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return responseHandler.responseSuccess("Xóa danh mục thành công", null);
    }
}