package com.example.j2ee_project.service.statistics;

import com.example.j2ee_project.entity.Bill;
import com.example.j2ee_project.entity.Order;
import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.model.dto.BillDTO;
import com.example.j2ee_project.model.dto.MealDTO;
import com.example.j2ee_project.model.dto.OrderDTO;
import com.example.j2ee_project.model.dto.OrderDetailDTO;
import com.example.j2ee_project.model.dto.RestaurantTableDTO;
import com.example.j2ee_project.model.dto.StatusDTO;
import com.example.j2ee_project.model.dto.UserDTO;
import com.example.j2ee_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.antlr.v4.runtime.misc.Utils.count;

@Service
@RequiredArgsConstructor
public class StatisticsService implements StatisticsServiceInterface {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final BillRepository billRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MealRepository mealRepository;
    private final ModelMapper modelMapper;

    @Override
    public Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.countByDateRange(start, end);
        long totalOrders = orderRepository.countByDateRange(start, end);
        long totalBills = billRepository.countByDateRange(start, end);
        var totalRevenue = billRepository.sumTotalAmountByDateRange(start, end);
        var averageRevenuePerBill = billRepository.averageAmountByDateRange(start, end);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalBookings", totalBookings);
        stats.put("totalOrders", totalOrders);
        stats.put("totalBills", totalBills);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : java.math.BigDecimal.ZERO);
        stats.put("averageRevenuePerBill", averageRevenuePerBill != null ? averageRevenuePerBill : java.math.BigDecimal.ZERO);

        return stats;
    }

    @Override
    public Page<MealDTO> getTopMeals(LocalDate startDate, LocalDate endDate, int limit, int offset) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        Pageable pageable = PageRequest.of(offset / limit, limit);

        Page<OrderDetailRepository.TopMealStats> page =
                orderDetailRepository.findTopMealsByDateRange(start, end, pageable);

        List<MealDTO> content = page.getContent().stream()
                .map(p -> MealDTO.builder()
                        .mealID(p.getMealID())
                        .mealName(p.getMealName())
                        .price(p.getPrice())
                        .image(p.getImage())
                        .categoryID(p.getCategoryID())
                        .categoryName(p.getCategoryName())
                        .statusId(p.getStatusId())
                        .totalOrdered(p.getTotalOrdered().intValue())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build())
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public Page<Map<String, Object>> getBookingStatistics(LocalDate startDate, LocalDate endDate, String type, int limit, int offset) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        org.springframework.data.domain.Page<java.lang.Object[]> page;
        if ("daily".equalsIgnoreCase(type)) {
            page = bookingRepository.getBookingStatsByDay(start, end, org.springframework.data.domain.PageRequest.of(offset / limit, limit));
        } else if ("monthly".equalsIgnoreCase(type)) {
            page = bookingRepository.getBookingStatsByMonth(start, end, org.springframework.data.domain.PageRequest.of(offset / limit, limit));
        } else {
            throw new IllegalArgumentException("Invalid type: " + type + ". Use 'daily' or 'monthly'");
        }

        List<Map<String, Object>> data = page.getContent().stream()
                .map(row -> Map.of(
                        "period", row[0],
                        "count", row[1]
                ))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(data, page.getPageable(), page.getTotalElements());
    }

    @Override
    public Page<Map<String, Object>> getBillStatistics(LocalDate startDate, LocalDate endDate, String type, int limit, int offset) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        org.springframework.data.domain.Page<java.lang.Object[]> page;
        if ("daily".equalsIgnoreCase(type)) {
            page = billRepository.getBillStatsByDay(start, end, org.springframework.data.domain.PageRequest.of(offset / limit, limit));
        } else if ("monthly".equalsIgnoreCase(type)) {
            page = billRepository.getBillStatsByMonth(start, end, org.springframework.data.domain.PageRequest.of(offset / limit, limit));
        } else {
            throw new IllegalArgumentException("Invalid type: " + type + ". Use 'daily' or 'monthly'");
        }

        List<Map<String, Object>> data = page.getContent().stream()
                .map(row -> Map.of(
                        "period", row[0],
                        "count", row[1],
                        "revenue", row[2]
                ))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(data, page.getPageable(), page.getTotalElements());
    }

    @Override
    public Map<String, Object> getMealStatistics(LocalDate startDate, LocalDate endDate) {
        long totalMeals = mealRepository.count();
        long activeMeals = mealRepository.findByFilters(null, 1, null, null, null, PageRequest.of(0, Integer.MAX_VALUE))
                .getTotalElements();

        return Map.of(
                "totalMeals", totalMeals,
                "activeMeals", activeMeals
        );
    }

//    @Override
//    public Map<String, Object> getOverviewDetails(LocalDate startDate, LocalDate endDate) {
//        LocalDateTime start = startDate.atStartOfDay();
//        LocalDateTime end = endDate.atTime(23, 59, 59);
//
//        List<com.example.j2ee_project.entity.User> users = userRepository.findAll();
//        List<Map<String, Object>> userDetails = new ArrayList<>();
//
//        for (User user : users) {
//            try {
//                List<Bill> bills = billRepository.findByUserIdAndDateRange(user.getUserID(), start, end);
//                List<Order> orders = orderRepository.findByUserIdAndDateRange(user.getUserID(), start, end);
//
//                UserDTO userDTO = modelMapper.map(user, UserDTO.class);
//                if (user.getStatus() != null) {
//                    userDTO.setStatusId(user.getStatus().getStatusID());
//                }
//
//                long numberOfBookings = bookingRepository.countByUserIdAndDateRange(user.getUserID(), start, end);
//                long numberOfOrders = orders.size();
//                long numberOfBills = bills.size();
//                long voucherUsage = bills.stream()
//                        .filter(b -> b.getVoucher() != null)
//                        .count();
//
//                        BigDecimal totalSpent = bills.stream()
//                        .map(com.example.j2ee_project.entity.Bill::getTotalAmount)
//                        .filter(java.util.Objects::nonNull)
//                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
//
//                Map<String, Integer> mealQuantities = new java.util.HashMap<>();
//                for (com.example.j2ee_project.entity.Order order : orders) {
//                    if (order.getOrderDetails() != null) {
//                        for (com.example.j2ee_project.entity.OrderDetail od : order.getOrderDetails()) {
//                            if (od.getMeal() != null) {
//                                String mealName = od.getMeal().getMealName();
//                                int qty = od.getQuantity();
//                                mealQuantities.put(mealName, mealQuantities.getOrDefault(mealName, 0) + qty);
//                            }
//                        }
//                    }
//                }
//
//                List<BillDTO> billDTOs = bills.stream()
//                        .map(bill -> modelMapper.map(bill, BillDTO.class))
//                        .collect(Collectors.toList());
//
//                List<Map<String, Object>> orderDetails = orders.stream()
//                        .map(order -> {
//                            Map<String, Object> orderMap = new HashMap<>();
//                            orderMap.put("order", modelMapper.map(order, OrderDTO.class));
//                            if (order.getRestaurantTable() != null) {
//                                orderMap.put("table", modelMapper.map(order.getRestaurantTable(), RestaurantTableDTO.class));
//                            } else {
//                                orderMap.put("table", null);
//                            }
//                            List<OrderDetailDTO> mealDetails = order.getOrderDetails() != null ? order.getOrderDetails().stream()
//                                    .map(od -> modelMapper.map(od, OrderDetailDTO.class))
//                                    .collect(Collectors.toList()) : new java.util.ArrayList<>();
//                            orderMap.put("meals", mealDetails);
//                            return orderMap;
//                        })
//                        .collect(Collectors.toList());
//
//                Map<String, Object> userDetail = new HashMap<>();
//                userDetail.put("user", userDTO);
//                userDetail.put("numberOfBookings", numberOfBookings);
//                userDetail.put("numberOfOrders", numberOfOrders);
//                userDetail.put("numberOfBills", numberOfBills);
//                userDetail.put("totalSpent", totalSpent);
//                userDetail.put("voucherUsage", voucherUsage);
//                userDetail.put("meals", mealQuantities);
//                userDetail.put("bills", billDTOs);
//                userDetail.put("orders", orderDetails);
//
//                userDetails.add(userDetail);
//            } catch (Exception e) {
//                // Skip this user if there's an error
//                continue;
//            }
//        }
//
//        return Map.of("userDetails", userDetails);
//    }
@Override
public Map<String, Object> getOverviewDetails(LocalDate startDate, LocalDate endDate, int limit, int offset) {
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.atTime(23, 59, 59);

    Map<String, Object> result = new HashMap<>();

    // 1. Tổng quan
    result.put("summary", Map.of(
        "totalCustomersVisited", orderRepository.countDistinctCustomersInPeriod(start, end),
        "totalBookings", bookingRepository.countByDateRange(start, end),
        "totalOrders", orderRepository.countByDateRange(start, end),
        "totalRevenue", billRepository.sumTotalAmountByDateRange(start, end),
        "averageSpendPerCustomer", billRepository.avgSpendPerCustomer(start, end),
        "customersUsedVoucher", billRepository.countCustomersUsedVoucher(start, end),
        "totalDiscountAmount", billRepository.sumDiscountAmount(start, end)
    ));

    // 2. Top khách hàng thân thiết
    List<Map<String, Object>> topCustomers = billRepository.findTopCustomers(start, end, org.springframework.data.domain.PageRequest.of(offset / limit, limit)).getContent().stream()
        .map(row -> {
            User user = (User) row[0];
            long visitCount = billRepository.countByUserIdAndDateRange(user.getUserID(), start, end);
            long bookingCount = bookingRepository.countByUserIdAndDateRange(user.getUserID(), start, end);
            return Map.of(
                "user", Map.of(
                    "userID", user.getUserID(),
                    "fullName", user.getFullName(),
                    "email", user.getEmail(),
                    "visitCount", visitCount,
                    "bookingCount", bookingCount
                ),
                "totalSpent", row[1]
            );
        })
        .collect(Collectors.toList());
    result.put("topCustomers", topCustomers);

    // 3. Món ăn được đặt nhiều nhất
    List<Map<String, Object>> popularDishes = orderDetailRepository.findTopOrderedMeals(start, end, org.springframework.data.domain.PageRequest.of(0, 10)).getContent().stream()
            .map(row -> {
                com.example.j2ee_project.entity.Meal meal = (com.example.j2ee_project.entity.Meal) row[0];
                Map<String, Object> mealMap = new HashMap<>();
                mealMap.put("mealID", meal.getMealID());
                mealMap.put("mealName", meal.getMealName());
                mealMap.put("price", meal.getPrice());
                mealMap.put("image", meal.getImage());
                mealMap.put("totalOrdered", row[1]);
                return mealMap;
            })
        .collect(Collectors.toList());
    result.put("popularDishes", popularDishes);

    // 4. Sử dụng mã giảm giá
    List<Map<String, Object>> voucherUsage = billRepository.getVoucherUsageStats(start, end).stream()
        .map(row -> Map.of(
            "voucherCode", row[0],
            "usageCount", row[1]
        ))
        .collect(Collectors.toList());
    result.put("voucherUsage", voucherUsage);

    return result;
}
}