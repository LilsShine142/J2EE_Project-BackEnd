package com.example.j2ee_project.service.meal;

import com.example.j2ee_project.model.dto.MealDTO;
import com.example.j2ee_project.model.request.meal.MealRequestDTO;
import org.springframework.data.domain.Page;

public interface MealServiceInterface {
    MealDTO createMeal(MealRequestDTO mealRequestDTO);

    Page<MealDTO> getAllMeals(int offset, int limit, String search, Integer statusId, Integer categoryId, Double minPrice, Double maxPrice);

    MealDTO getMealById(Integer mealID);

    MealDTO updateMeal(Integer mealID, MealRequestDTO mealRequestDTO);

    void deleteMeal(Integer mealID);
}