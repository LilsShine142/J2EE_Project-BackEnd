package com.example.j2ee_project.service.meal;

import com.example.j2ee_project.model.dto.MealDTO;
import com.example.j2ee_project.model.request.meal.MealRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface MealServiceInterface {
    MealDTO createMeal(MealRequestDTO mealRequestDTO);

    Page<MealDTO> getAllMeals(int offset, int limit, String search, Integer statusId, Integer categoryId, Double minPrice, Double maxPrice);

    MealDTO getMealById(Integer mealID);

    MealDTO updateMeal(Integer mealID, MealRequestDTO mealRequestDTO);

    void deleteMeal(Integer mealID);

    List<MealDTO> getTopPopular(@RequestParam(defaultValue = "9") int limit);

    /**
     * Lấy danh sách món ăn theo categoryId với các bộ lọc
     */
    Page<MealDTO> getMealsByCategoryId(
            Integer categoryId,
            int offset,
            int limit,
            String search,
            Integer statusId,
            Double minPrice,
            Double maxPrice);
}