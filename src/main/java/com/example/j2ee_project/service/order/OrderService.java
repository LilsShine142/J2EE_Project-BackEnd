package com.example.j2ee_project.service.order;

import com.example.j2ee_project.entity.*;
import com.example.j2ee_project.entity.keys.KeyOrderDetailId;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.OrderDTO;
import com.example.j2ee_project.model.dto.OrderDetailDTO;
import com.example.j2ee_project.model.request.order.OrderRequestDTO;
import com.example.j2ee_project.model.request.order.OrderDetailRequest;
import com.example.j2ee_project.repository.*;
import com.example.j2ee_project.utils._enum.EStatus;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final RestaurantTableRepository tableRepository;
    private final MealRepository mealRepository;
    private final StatusRepository statusRepository;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderRequestDTO orderRequestDTO) {
        try {
//            log.info("Creating order for userID: {}", orderRequestDTO.getUserID());

            // Validate user
            User user = userRepository.findById(orderRequestDTO.getUserID())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy user với ID: " + orderRequestDTO.getUserID()));

            // Tạo Order entity
            Order order = new Order();
            order.setUser(user);

            // Xử lý bookingID
            if (orderRequestDTO.getBookingID() != null) {
                order.setBookingID(orderRequestDTO.getBookingID());
            }

            // Xử lý table
            if (orderRequestDTO.getTableID() != null) {
                RestaurantTable table = tableRepository.findById(orderRequestDTO.getTableID())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy bàn với ID: " + orderRequestDTO.getTableID()));
                order.setRestaurantTable(table);
            }

            // Set các field cơ bản
            order.setOrderDate(LocalDateTime.now());

            // Lấy status (mặc định là PENDING nếu không có)
            Integer statusId = orderRequestDTO.getStatusId() != null ? orderRequestDTO.getStatusId() : 1;
            Status status = statusRepository.findById(statusId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái với ID: " + statusId));
            order.setStatus(status);

            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            // Lưu Order trước để có orderID
            Order savedOrder = orderRepository.save(order);
//            log.info("Created order with ID: {}", savedOrder.getOrderID());

            // Thêm order details
            if (orderRequestDTO.getOrderDetail() != null && !orderRequestDTO.getOrderDetail().isEmpty()) {
                for (OrderDetailRequest detailRequest : orderRequestDTO.getOrderDetail()) {
//                    log.info("Adding order detail - mealID: {}, quantity: {}",
//                            detailRequest.getMealID(), detailRequest.getQuantity());
                    addOrderDetail(savedOrder.getOrderID(), detailRequest);
                }
            }

            // Refresh để lấy đầy đủ thông tin
            Order finalOrder = orderRepository.findById(savedOrder.getOrderID())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found after creation"));

            return convertToDTO(finalOrder);

        } catch (Exception e) {
//            log.error("Error creating order: ", e);
            throw new RuntimeException("Đã xảy ra lỗi khi tạo order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public OrderDetailDTO addOrderDetail(Integer orderID, OrderDetailRequest request) {
//        log.info("Adding order detail - orderID: {}, mealID: {}", orderID, request.getMealID());

        // Tìm order
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderID));

        // Tìm meal
        Meal meal = mealRepository.findById(request.getMealID())
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with ID: " + request.getMealID()));

        // Kiểm tra trạng thái món ăn
        if (!meal.getStatus().getStatusName().equals(EStatus.ACTIVE.getName())) {
            throw new IllegalStateException("Món ăn " + meal.getMealName() + " không khả dụng");
        }

        // Kiểm tra quantity
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng món ăn phải lớn hơn 0");
        }

        // Kiểm tra xem order detail đã tồn tại chưa
        Optional<OrderDetail> existingDetail = orderDetailRepository
                .findByOrderOrderIDAndMealMealID(orderID, request.getMealID());

        OrderDetail orderDetail;

        if (existingDetail.isPresent()) {
            // Update existing detail
            orderDetail = existingDetail.get();
            int newQuantity = orderDetail.getQuantity() + request.getQuantity();
            orderDetail.setQuantity(newQuantity);

            // Tính lại subtotal
            BigDecimal newSubTotal = meal.getPrice().multiply(BigDecimal.valueOf(newQuantity));
            orderDetail.setSubTotal(newSubTotal);
            orderDetail.setUpdatedAt(LocalDateTime.now());

//            log.info("Updated existing order detail - new quantity: {}, new subtotal: {}",
//                    newQuantity, newSubTotal);
        } else {
            // Tạo mới order detail với composite key
            KeyOrderDetailId key = new KeyOrderDetailId(orderID, request.getMealID());
            BigDecimal subTotal = meal.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

            orderDetail = OrderDetail.builder()
                    .id(key)
                    .order(order)
                    .meal(meal)
                    .quantity(request.getQuantity())
                    .subTotal(subTotal)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

//            log.info("Created new order detail - quantity: {}, subtotal: {}",
//                    request.getQuantity(), subTotal);
        }

        // Lưu order detail
        OrderDetail savedDetail = orderDetailRepository.save(orderDetail);
//        log.info("Successfully saved order detail");

        return convertDetailToDTO(savedDetail);
    }

    @Override
    @Transactional
    public void saveOrderDetails(Integer orderID, List<OrderDetailDTO> detailDTOs) {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order với ID: " + orderID));

        for (OrderDetailDTO dto : detailDTOs) {
            Meal meal = mealRepository.findById(dto.getMealID())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + dto.getMealID()));

            BigDecimal subTotal = meal.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));

            // Tạo composite key
            KeyOrderDetailId key = new KeyOrderDetailId(orderID, dto.getMealID());

            OrderDetail detail = OrderDetail.builder()
                    .id(key)
                    .order(order)
                    .meal(meal)
                    .quantity(dto.getQuantity())
                    .subTotal(subTotal)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            orderDetailRepository.save(detail);
        }
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getOrdersPaginated(int offset, int limit,
            Integer userID,
            Integer statusId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        // Validate input parameters
        if (offset < 0)
            offset = 0;
        if (limit <= 0)
            limit = 10;
        if (limit > 100)
            limit = 100;

        Pageable pageable = PageRequest.of(offset / limit, limit);

        Page<Order> page;

        // Sửa lỗi: Sử dụng các methods đúng từ repository
        if (userID != null && statusId != null && startDate != null && endDate != null) {
            page = orderRepository.findByUserUserIDAndStatusAndOrderDateBetween(userID, statusId, startDate, endDate,
                    pageable);
        } else if (userID != null && statusId != null) {
            page = orderRepository.findByUserUserIDAndStatus(userID, statusId, pageable);
        } else if (userID != null && startDate != null && endDate != null) {
            page = orderRepository.findByUserUserIDAndOrderDateBetween(userID, startDate, endDate, pageable);
        } else if (statusId != null && startDate != null && endDate != null) {
            page = orderRepository.findByStatusAndOrderDateBetween(statusId, startDate, endDate, pageable);
        } else if (userID != null) {
            page = orderRepository.findByUserUserID(userID, pageable);
        } else if (statusId != null) {
            page = orderRepository.findByStatus(statusId, pageable);
        } else if (startDate != null && endDate != null) {
            page = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }

        List<OrderDTO> orders = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orders);
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("hasNext", page.hasNext());
        response.put("hasPrevious", page.hasPrevious());
        return response;
    }

    @Override
    public OrderDTO getOrderById(Integer orderID) {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order với ID: " + orderID));
        return convertToDTO(order);
    }

    @Override
    public List<OrderDTO> getOrdersByUser(Integer userID) {
        // Sửa lỗi: Sử dụng method đúng từ repository
        return orderRepository.findByUserUserID(userID).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByStatus(Status status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByDate(LocalDateTime date) {
        // Tìm orders trong ngày cụ thể
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return orderRepository.findByOrderDateBetween(startOfDay, endOfDay).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDetailDTO> getOrderDetails(Integer orderID) {
        // Sửa lỗi: Sử dụng method đúng từ repository
        return orderDetailRepository.findByOrderOrderID(orderID).stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrderStatus(Integer orderID, Status status) {
        return null;
    }

    // @Override
    // @Transactional
    // public OrderDTO updateOrderStatus(Integer orderID, Status status) {
    // Order order = orderRepository.findById(orderID)
    // .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order với
    // ID: " + orderID));

    // order.setStatus(status);
    // Order updatedOrder = orderRepository.save(order);
    // return convertToDTO(updatedOrder);
    // }

    @Override
    @Transactional
    public void deleteOrder(Integer orderID) {
        if (!orderRepository.existsById(orderID)) {
            throw new ResourceNotFoundException("Không tìm thấy order với ID: " + orderID);
        }
        // Xóa order details trước
        orderDetailRepository.deleteByOrderOrderID(orderID);
        // Xóa order
        orderRepository.deleteById(orderID);
    }

    @Override
    public Double getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal total = orderRepository.getTotalRevenueByDateRange(startDate, endDate);
        return total != null ? total.doubleValue() : 0.0;
    }

    // @Override
    // public Map<String, Object> getRevenueReport(LocalDateTime startDate,
    // LocalDateTime endDate) {
    // Double totalRevenue = getTotalRevenue(startDate, endDate);
    // List<Order> orders = orderRepository.findByOrderDateBetween(startDate,
    // endDate);

    // Map<String, Object> report = new HashMap<>();
    // report.put("totalRevenue", totalRevenue);
    // report.put("totalOrders", orders.size());
    // report.put("startDate", startDate);
    // report.put("endDate", endDate);

    // // Thêm thông tin chi tiết
    // Map<String, Long> statusCount = orders.stream()
    // .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    // report.put("ordersByStatus", statusCount);

    // return report;
    // }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderID(order.getOrderID());
        dto.setUserID(order.getUser().getUserID());
        dto.setUserName(order.getUser().getFullName());
        dto.setBookingID(order.getBookingID());

        if (order.getRestaurantTable() != null) {
            dto.setTableID(order.getRestaurantTable().getTableID());
            dto.setTableName(order.getRestaurantTable().getTableName());
        }

        dto.setOrderDate(order.getOrderDate());
        dto.setStatusId(order.getStatus().getStatusID());
//        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        // Tính tổng tiền từ order details
        BigDecimal total = orderDetailRepository.sumSubTotalByOrderOrderID(order.getOrderID());
        dto.setTotalAmount(total != null ? total.doubleValue() : 0.0);

        return dto;
    }

    private OrderDetailDTO convertDetailToDTO(OrderDetail detail) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setOrderID(detail.getOrder().getOrderID());
        dto.setMealID(detail.getMeal().getMealID());
//        dto.setMealName(detail.getMeal().getMealName());
        dto.setQuantity(detail.getQuantity());
        // dto.setUnitPrice(detail.getMeal().getPrice().doubleValue());
        dto.setSubTotal(detail.getSubTotal().doubleValue());
//        dto.setCreateAt(LocalDateTime.now());
        dto.setUpdateAt(LocalDateTime.now());
        return dto;
    }

}