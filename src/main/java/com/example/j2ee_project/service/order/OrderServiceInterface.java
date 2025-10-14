package com.example.j2ee_project.service.order;

import com.example.j2ee_project.model.dto.OrderDTO;
import com.example.j2ee_project.model.request.order.OrderRequestDTO;
import org.springframework.data.domain.Page;

public interface OrderServiceInterface {
    OrderDTO createOrder(OrderRequestDTO orderRequestDTO);

    Page<OrderDTO> getAllOrders(int offset, int limit, String search, Integer statusId, Integer userId, Integer tableId);

    OrderDTO getOrderById(Integer orderId);

    OrderDTO updateOrder(Integer orderId, OrderRequestDTO orderRequestDTO);

    void deleteOrder(Integer orderId);
}