package com.example.j2ee_project.service.order;

import com.example.j2ee_project.entity.*;
import com.example.j2ee_project.entity.keys.KeyOrderDetailId;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.OrderDTO;
import com.example.j2ee_project.model.dto.OrderDetailDTO;
import com.example.j2ee_project.model.request.order.OrderDetailRequest;
import com.example.j2ee_project.model.request.order.OrderRequestDTO;
import com.example.j2ee_project.repository.MealRepository;
import com.example.j2ee_project.repository.OrderDetailRepository;
import com.example.j2ee_project.repository.OrderRepository;
import com.example.j2ee_project.repository.RestaurantTableRepository;
import com.example.j2ee_project.repository.StatusRepository;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.utils._enum.EStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderServiceInterface {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final MealRepository mealRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StatusRepository statusRepository;

    private List<OrderDetail> addOrderDetails;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderRequestDTO orderRequestDTO) {
        // Validate user
        User user = userRepository.findById(orderRequestDTO.getUserID())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + orderRequestDTO.getUserID()));

        // Validate table
        RestaurantTable table = restaurantTableRepository.findById(orderRequestDTO.getTableID())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + orderRequestDTO.getTableID()));

        // Check table status
        if (!table.getStatus().getStatusName().equals(EStatus.AVAILABLE.getName())) {
            throw new IllegalStateException("Bàn không sẵn sàng để đặt");
        }

        // Validate status (default to PENDING)
        Status status = statusRepository.findById(orderRequestDTO.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái với ID: " + orderRequestDTO.getStatusId()));

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setBookingID(orderRequestDTO.getBookingID());
        order.setRestaurantTable(table);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Handle order details if provided
        if (orderRequestDTO.getOrderDetails() != null && !orderRequestDTO.getOrderDetails().isEmpty()) {
            addOrderDetails = new ArrayList<>();
            OrderDetail saveOrderDetail;
            for (OrderDetailRequest detailRequest : orderRequestDTO.getOrderDetails()) {
                Meal meal = mealRepository.findById(detailRequest.getMealID())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + detailRequest.getMealID()));

                OrderDetail detail = OrderDetail.builder()
                        .id(new KeyOrderDetailId(savedOrder.getOrderID(), detailRequest.getMealID())) // Temporary orderID
                        .order(savedOrder)
                        .meal(meal)
                        .quantity(detailRequest.getQuantity())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                detail.calculateSubTotal();
                saveOrderDetail = orderDetailRepository.save(detail);
                addOrderDetails.add(saveOrderDetail);
            }
        }

        // Calculate totalAmount
        double totalAmount = savedOrder.getOrderDetails().stream()
                .mapToDouble(detail -> detail.getSubTotal().doubleValue())
                .sum();

        savedOrder.setOrderDetails(addOrderDetails);
        return mapToOrderDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(int offset, int limit, String search, Integer statusId, Integer userId, Integer tableId) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;

        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);

        Page<Order> orderPage = orderRepository.findByFilters(search, statusId, userId, tableId, pageable);

        return orderPage.map(order -> {
            double totalAmount = order.getOrderDetails().stream()
                    .mapToDouble(detail -> detail.getSubTotal().doubleValue())
                    .sum();
            return mapToOrderDTO(order);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        double totalAmount = order.getOrderDetails().stream()
                .mapToDouble(detail -> detail.getSubTotal().doubleValue())
                .sum();

        return mapToOrderDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(Integer orderId, OrderRequestDTO orderRequestDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Check if update is allowed (within 2 hours from createdAt)
        LocalDateTime now = LocalDateTime.now();
        if (order.getCreatedAt().plusHours(2).isBefore(now)) {
            throw new IllegalStateException("Không thể sửa đổi đơn hàng sau 2 giờ kể từ khi tạo");
        }

        // Update user if provided
        if (orderRequestDTO.getUserID() != null) {
            User user = userRepository.findById(orderRequestDTO.getUserID())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + orderRequestDTO.getUserID()));
            order.setUser(user);
        }

        // Update bookingID if provided
        if (orderRequestDTO.getBookingID() != null) {
            order.setBookingID(orderRequestDTO.getBookingID());
        }

        // Update table if provided
        if (orderRequestDTO.getTableID() != null) {
            RestaurantTable table = restaurantTableRepository.findById(orderRequestDTO.getTableID())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + orderRequestDTO.getTableID()));

            // Check table status
            if (!table.getStatus().getStatusName().equals(EStatus.AVAILABLE.getName())) {
                throw new IllegalStateException("Bàn không sẵn sàng để đặt");
            }

            order.setRestaurantTable(table);
        }

        // Update status if provided
        if (orderRequestDTO.getStatusId() != null) {
            Status status = statusRepository.findById(orderRequestDTO.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái với ID: " + orderRequestDTO.getStatusId()));
            order.setStatus(status);
        }

        // Update order details if provided
        if (orderRequestDTO.getOrderDetails() != null) {
            // Remove existing order details
            orderDetailRepository.deleteByOrderId(orderId);

            // Add new order details
            List<OrderDetail> newOrderDetails = new ArrayList<>();
            // Validate for duplicate mealIDs
            List<Integer> mealIds = orderRequestDTO.getOrderDetails().stream()
                    .map(OrderDetailRequest::getMealID)
                    .collect(Collectors.toList());
            if (mealIds.stream().distinct().count() != mealIds.size()) {
                throw new IllegalStateException("Danh sách món ăn chứa mealID trùng lặp");
            }

            for (OrderDetailRequest detailRequest : orderRequestDTO.getOrderDetails()) {
                Meal meal = mealRepository.findById(detailRequest.getMealID())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + detailRequest.getMealID()));

                OrderDetail detail = OrderDetail.builder()
                        .id(new KeyOrderDetailId(orderId, detailRequest.getMealID()))
                        .order(order)
                        .meal(meal)
                        .quantity(detailRequest.getQuantity())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                detail.calculateSubTotal();
                newOrderDetails.add(detail);
            }
            order.setOrderDetails(newOrderDetails);
        }

        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        double totalAmount = updatedOrder.getOrderDetails().stream()
                .mapToDouble(detail -> detail.getSubTotal().doubleValue())
                .sum();

        return mapToOrderDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Check if cancellation is allowed (within 2 hours from createdAt)
        LocalDateTime now = LocalDateTime.now();
        if (order.getCreatedAt().plusHours(2).isBefore(now)) {
            throw new IllegalStateException("Không thể hủy đơn hàng sau 2 giờ kể từ khi tạo");
        }

        // Set status to CANCELLED instead of deleting
        Status cancelledStatus = statusRepository.findByStatusName(EStatus.CANCELLED.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CANCELLED"));
        order.setStatus(cancelledStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private OrderDTO mapToOrderDTO(Order order) {
        List<OrderDetailDTO> detailDTOs = order.getOrderDetails() != null
                ? order.getOrderDetails().stream()
                .map(detail -> OrderDetailDTO.builder()
                        .orderID(detail.getOrder().getOrderID())
                        .mealID(detail.getMeal().getMealID())
                        .mealName(detail.getMeal().getMealName())
                        .mealPrice(detail.getMeal().getPrice())
                        .quantity(detail.getQuantity())
                        .subTotal(detail.getSubTotal())
                        .createAt(detail.getCreatedAt())
                        .updateAt(detail.getUpdatedAt())
                        .build())
                .collect(Collectors.toList())
                : null;

        return OrderDTO.builder()
                .orderID(order.getOrderID())
                .userID(order.getUser().getUserID())
                .userName(order.getUser().getFullName())
                .bookingID(order.getBookingID())
                .tableID(order.getRestaurantTable().getTableID())
                .tableName(order.getRestaurantTable().getTableName())
                .orderDate(order.getOrderDate())
                .statusId(order.getStatus().getStatusID())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderDetails(detailDTOs)
                .build();
    }
}