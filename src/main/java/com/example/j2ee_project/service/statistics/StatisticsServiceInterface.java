package com.example.j2ee_project.service.statistics;

import com.example.j2ee_project.model.dto.MealDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatisticsServiceInterface {
    Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate);
    Page<MealDTO> getTopMeals(LocalDate startDate, LocalDate endDate, int limit, int offset);
    Page<Map<String, Object>> getBookingStatistics(LocalDate startDate, LocalDate endDate, String type, int limit, int offset);
    Page<Map<String, Object>> getBillStatistics(LocalDate startDate, LocalDate endDate, String type, int limit, int offset);
    Map<String, Object> getMealStatistics(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getOverviewDetails(LocalDate startDate, LocalDate endDate, int limit, int offset);
}
