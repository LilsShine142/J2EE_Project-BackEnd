package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Meal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface MealRepository extends JpaRepository<Meal, Integer> {
        boolean existsByMealName(String mealName);

        @Query("SELECT m FROM Meal m WHERE " +
                "(:search IS NULL OR LOWER(m.mealName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                "(:statusId IS NULL OR m.status.statusID = :statusId) AND " +
                "(:categoryId IS NULL OR m.category.categoryID = :categoryId) AND " +
                "(:minPrice IS NULL OR m.price >= :minPrice) AND " +
                "(:maxPrice IS NULL OR m.price <= :maxPrice)")
        Page<Meal> findByFilters(
                @Param("search") String search,
                @Param("statusId") Integer statusId,
                @Param("categoryId") Integer categoryId,
                @Param("minPrice") BigDecimal minPrice,
                @Param("maxPrice") BigDecimal maxPrice,
                Pageable pageable);
}