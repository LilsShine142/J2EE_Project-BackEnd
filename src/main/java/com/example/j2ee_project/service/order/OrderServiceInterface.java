package com.example.j2ee_project.service.order;

import com.example.j2ee_project.entity.Status;
import com.example.j2ee_project.model.dto.OrderDTO;
import com.example.j2ee_project.model.dto.OrderDetailDTO;
import com.example.j2ee_project.model.request.order.OrderDetailRequest;
import com.example.j2ee_project.model.request.order.OrderRequestDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderServiceInterface {

    OrderDTO createOrder(OrderRequestDTO orderRequestDTO);

    OrderDetailDTO addOrderDetail(Integer orderID, OrderDetailRequest request);

    void saveOrderDetails(Integer orderID, List<OrderDetailDTO> detailDTOs);

    List<OrderDTO> getAllOrders();

    Map<String, Object> getOrdersPaginated(int offset, int limit,
                                           Integer userID,
                                           Integer statusId,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate);

    OrderDTO getOrderById(Integer orderID);

    List<OrderDTO> getOrdersByUser(Integer userID);

    List<OrderDTO> getOrdersByStatus(Status status); // UNCOMMENT VÀ SỬA

    List<OrderDTO> getOrdersByDate(LocalDateTime date);

    List<OrderDetailDTO> getOrderDetails(Integer orderID);

    OrderDTO updateOrderStatus(Integer orderID, Status status); // UNCOMMENT VÀ SỬA

    void deleteOrder(Integer orderID);

    Double getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);

    // Map<String, Object> getRevenueReport(LocalDateTime startDate, LocalDateTime endDate);
}