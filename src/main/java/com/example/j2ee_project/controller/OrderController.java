package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.OrderDTO;
import com.example.j2ee_project.model.request.order.OrderRequestDTO;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final ResponseHandler responseHandler;

    @Autowired
    public OrderController(OrderService orderService, ResponseHandler responseHandler) {
        this.orderService = orderService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        OrderDTO response = orderService.createOrder(orderRequestDTO);
        return responseHandler.responseCreated("Tạo đơn hàng thành công", response);
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer tableId) {
        Page<OrderDTO> orderPage = orderService.getAllOrders(offset, limit, search, statusId, userId, tableId);
        return responseHandler.responseSuccess("Lấy danh sách đơn hàng thành công", orderPage);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Integer orderId) {
        OrderDTO response = orderService.getOrderById(orderId);
        return responseHandler.responseSuccess("Lấy thông tin đơn hàng thành công", response);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(@PathVariable Integer orderId, @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        OrderDTO response = orderService.updateOrder(orderId, orderRequestDTO);
        return responseHandler.responseSuccess("Cập nhật đơn hàng thành công", response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Integer orderId) {
        orderService.deleteOrder(orderId);
        return responseHandler.responseSuccess("Hủy đơn hàng thành công", null);
    }
}