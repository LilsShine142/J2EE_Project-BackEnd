package com.example.j2ee_project.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.j2ee_project.model.dto.MealDTO;
import com.example.j2ee_project.model.request.meal.MealRequestDTO;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.meal.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
@Tag(name = "Meal Management", description = "APIs for managing restaurant meals and menu items")
@RequiredArgsConstructor
public class MealController {
    private final MealService mealService;
    private final ResponseHandler responseHandler;

    @PostMapping("/create")
    public ResponseEntity<?> createMeal(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody MealRequestDTO mealRequestDTO) {
        MealDTO response = mealService.createMeal(token, mealRequestDTO);
        return responseHandler.responseCreated("Tạo món ăn thành công", response);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllMeals(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        Page<MealDTO> mealPage = mealService.getAllMeals(token, offset, limit, search, statusId, categoryId, minPrice, maxPrice);
        return responseHandler.responseSuccess("Lấy danh sách món ăn thành công", mealPage);
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularMeals(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "9") int limit) {

        List<MealDTO> topMeals = mealService.getTopPopular(token, limit);
        return responseHandler.responseSuccess("Lấy top món phổ biến thành công", topMeals);
    }

    @GetMapping("/{mealID}")
    public ResponseEntity<?> getMealById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Integer mealID) {
        MealDTO response = mealService.getMealById(token, mealID);
        return responseHandler.responseSuccess("Lấy món ăn thành công", response);
    }

    @PutMapping("/update/{mealID}")
    public ResponseEntity<?> updateMeal(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Integer mealID, @Valid @RequestBody MealRequestDTO mealRequestDTO) {
        MealDTO response = mealService.updateMeal(token, mealID, mealRequestDTO);
        return responseHandler.responseSuccess("Cập nhật món ăn thành công", response);
    }

    @DeleteMapping("/delete/{mealID}")
    public ResponseEntity<?> deleteMeal(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Integer mealID) {
        mealService.deleteMeal(token, mealID);
        return responseHandler.responseSuccess("Xóa món ăn thành công", null);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getMealsByCategoryId(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        Page<MealDTO> mealPage = mealService.getMealsByCategoryId(
                token, categoryId, offset, limit, search, statusId, minPrice, maxPrice);

        return responseHandler.responseSuccess("Lấy danh sách món ăn theo danh mục thành công", mealPage);
    }
}
