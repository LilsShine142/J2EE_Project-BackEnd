package com.example.j2ee_project.service.meal;

import com.example.j2ee_project.entity.Category;
import com.example.j2ee_project.entity.Meal;
import com.example.j2ee_project.entity.Status;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.exception.DuplicateResourceException;
import com.example.j2ee_project.model.dto.MealDTO;
import com.example.j2ee_project.model.request.meal.MealRequestDTO;
import com.example.j2ee_project.repository.CategoryRepository;
import com.example.j2ee_project.repository.MealRepository;
import com.example.j2ee_project.repository.StatusRepository;
import com.example.j2ee_project.utils._enum.EPermission;
import com.example.j2ee_project.utils.role_permission.RolePermissionUtils;
import com.example.j2ee_project.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.example.j2ee_project.service.log.LogServiceInterface;
import com.example.j2ee_project.model.request.log.LogRequest;

@Service
@RequiredArgsConstructor
public class MealService implements MealServiceInterface {

    private final MealRepository mealRepository;
    private final CategoryRepository categoryRepository;
    private final StatusRepository statusRepository;
    private final RolePermissionUtils rolePermissionUtils;
    private final LogServiceInterface logService;

    @Override
    @Transactional
    public MealDTO createMeal(String token, MealRequestDTO mealRequestDTO) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để tạo món ăn!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.CREATE_MEAL.getCode())) {
            throw new ForbiddenException("Bạn không có quyền tạo món ăn!");
        }
        if (mealRepository.existsByMealName(mealRequestDTO.getMealName())) {
            throw new DuplicateResourceException("Tên món ăn đã tồn tại: " + mealRequestDTO.getMealName());
        }

        Category category = categoryRepository.findById(mealRequestDTO.getCategoryID())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy category với ID: " + mealRequestDTO.getCategoryID()));

        Status status = statusRepository.findById(mealRequestDTO.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy status với ID: " + mealRequestDTO.getStatusId()));

        Meal meal = new Meal();
        meal.setMealName(mealRequestDTO.getMealName());
        meal.setImage(mealRequestDTO.getImage());
        meal.setPrice(BigDecimal.valueOf(mealRequestDTO.getPrice()));
        meal.setCategory(category);
        meal.setStatus(status);
        meal.setCreatedAt(LocalDateTime.now());
        meal.setUpdatedAt(LocalDateTime.now());

        Meal savedMeal = mealRepository.save(meal);
        Integer currentUserId = rolePermissionUtils.getUserIdFromToken(token);
        logService.createLog(new LogRequest("meals", savedMeal.getMealID(), "CREATE", "Created meal with ID: " + savedMeal.getMealID(), currentUserId));
        return mapToMealDTO(savedMeal);
    }

    @Override
    public Page<MealDTO> getAllMeals(String token, int offset, int limit, String search, Integer statusId, Integer categoryId, Double minPrice, Double maxPrice) {
        if (token != null && !rolePermissionUtils.hasPermission(token, EPermission.VIEW_MEAL.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem danh sách món ăn!");
        }

        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;

        if (search == null) search = "";

        // Validate price range
        if (minPrice != null && maxPrice != null) {
            if (minPrice < 0 || maxPrice < 0) {
                throw new IllegalArgumentException("Giá không được âm");
            }
            if (minPrice > maxPrice) {
                throw new IllegalArgumentException("Giá tối thiểu không được lớn hơn giá tối đa");
            }
        }

        Pageable pageable = PageRequest.of(offset / limit, limit);

        BigDecimal minPriceBD = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxPriceBD = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        Page<Meal> mealPage = mealRepository.findByFilters(search, statusId, categoryId, minPriceBD, maxPriceBD, pageable);

        return mealPage.map(this::mapToMealDTO);
    }

    @Override
    public MealDTO getMealById(String token, Integer mealID) {
        if (token != null && !rolePermissionUtils.hasPermission(token, EPermission.VIEW_MEAL.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem món ăn!");
        }
        Meal meal = mealRepository.findById(mealID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + mealID));
        return mapToMealDTO(meal);
    }

    @Override
    @Transactional
    public MealDTO updateMeal(String token, Integer mealID, MealRequestDTO mealRequestDTO) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để cập nhật món ăn!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.UPDATE_MEAL.getCode())) {
            throw new ForbiddenException("Bạn không có quyền cập nhật món ăn!");
        }
        Meal meal = mealRepository.findById(mealID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + mealID));

        if (mealRequestDTO.getMealName() != null &&
                !mealRequestDTO.getMealName().equals(meal.getMealName()) &&
                mealRepository.existsByMealName(mealRequestDTO.getMealName())) {
            throw new DuplicateResourceException("Tên món ăn đã tồn tại: " + mealRequestDTO.getMealName());
        }

        if (mealRequestDTO.getMealName() != null) {
            meal.setMealName(mealRequestDTO.getMealName());
        }
        if (mealRequestDTO.getPrice() != null) {
            meal.setPrice(BigDecimal.valueOf(mealRequestDTO.getPrice()));
        }
        if (mealRequestDTO.getImage() != null) {
            meal.setImage(mealRequestDTO.getImage());
        }
        if (mealRequestDTO.getCategoryID() != null) {
            Category category = categoryRepository.findById(mealRequestDTO.getCategoryID())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy category với ID: " + mealRequestDTO.getCategoryID()));
            meal.setCategory(category);
        }
        if (mealRequestDTO.getStatusId() != null) {
            Status status = statusRepository.findById(mealRequestDTO.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy status với ID: " + mealRequestDTO.getStatusId()));
            meal.setStatus(status);
        }

        meal.setUpdatedAt(LocalDateTime.now());
        Meal updatedMeal = mealRepository.save(meal);
        Integer currentUserId = rolePermissionUtils.getUserIdFromToken(token);
        logService.createLog(new LogRequest("meals", mealID, "UPDATE", "Updated meal with ID: " + mealID, currentUserId));
        return mapToMealDTO(updatedMeal);
    }

    @Override
    @Transactional
    public void deleteMeal(String token, Integer mealID) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để xóa món ăn!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.DELETE_MEAL.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xóa món ăn!");
        }
        if (!mealRepository.existsById(mealID)) {
            throw new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + mealID);
        }
        mealRepository.deleteById(mealID);
        Integer currentUserId = rolePermissionUtils.getUserIdFromToken(token);
        logService.createLog(new LogRequest("meals", mealID, "DELETE", "Deleted meal with ID: " + mealID, currentUserId));
    }

    @Override
    public Page<MealDTO> getMealsByCategoryId(
            String token,
            Integer categoryId,
            int offset, int limit,
            String search, Integer statusId,
            Double minPrice, Double maxPrice) {

        if (token != null && !rolePermissionUtils.hasPermission(token, EPermission.VIEW_MEAL.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem món ăn theo danh mục!");
        }

        // Validate categoryId
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId là bắt buộc");
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Không tìm thấy category với ID: " + categoryId);
        }

        // Chuẩn hóa tham số phân trang & tìm kiếm
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        // Validate price
        if (minPrice != null && maxPrice != null) {
            if (minPrice < 0 || maxPrice < 0) {
                throw new IllegalArgumentException("Giá không được âm");
            }
            if (minPrice > maxPrice) {
                throw new IllegalArgumentException("Giá tối thiểu không được lớn hơn giá tối đa");
            }
        }

        Pageable pageable = PageRequest.of(offset / limit, limit);

        BigDecimal minPriceBD = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxPriceBD = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        // Gọi query với categoryId bắt buộc, các filter khác optional
        Page<Meal> mealPage = mealRepository.findByFilters(
                search, statusId, categoryId, minPriceBD, maxPriceBD, pageable);

        return mealPage.map(this::mapToMealDTO);
    }

    @Override
    public List<MealDTO> getTopPopular(String token, int limit) {
        if (token != null && !rolePermissionUtils.hasPermission(token, EPermission.VIEW_MEAL.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem món ăn phổ biến!");
        }

        if (limit < 1 || limit > 50) limit = 9; // Mặc định 9

        Pageable pageable = PageRequest.of(0, limit);
        Page<Meal> topMeals = mealRepository.findTopPopular(pageable);

        return topMeals.getContent().stream()
                .map(this::mapToMealDTO)
                .collect(Collectors.toList());
    }

    private MealDTO mapToMealDTO(Meal meal) {
        MealDTO.MealDTOBuilder builder = MealDTO.builder()
                .mealID(meal.getMealID())
                .mealName(meal.getMealName())
                .price(meal.getPrice().doubleValue())
                .image(meal.getImage())
                .categoryID(meal.getCategory().getCategoryID())
                .categoryName(meal.getCategory().getCategoryName())
                .statusId(meal.getStatus().getStatusID())
                .totalOrdered(meal.getTotalOrdered())
                .createdAt(meal.getCreatedAt())
                .updatedAt(meal.getUpdatedAt());
        return builder.build();
    }
}