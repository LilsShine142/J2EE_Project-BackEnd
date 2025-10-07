package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.OrderDTO;
import com.example.j2ee_project.model.dto.OrderDetailDTO;
import com.example.j2ee_project.model.request.order.OrderRequestDTO;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.order.OrderService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        try {
            OrderDTO createdOrder = orderService.createOrder(orderRequestDTO);
            return responseHandler.responseCreated("Tạo đơn hàng thành công", createdOrder);
        } catch (IllegalStateException ex) {
            return responseHandler.responseError(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
            return responseHandler.responseError("Đã xảy ra lỗi khi tạo đặt bàn: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getOrdersPaginated(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer userID,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> response = orderService.getOrdersPaginated(offset, limit, userID, statusId, startDate,
                endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderID}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer orderID) {
        OrderDTO order = orderService.getOrderById(orderID);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userID}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable Integer userID) {
        List<OrderDTO> orders = orderService.getOrdersByUser(userID);
        return ResponseEntity.ok(orders);
    }

    // @GetMapping("/status/{statusId}")
    // public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable Integer
    // statusId) {
    // List<OrderDTO> orders = orderService.getOrdersByStatus(statusId);
    // return ResponseEntity.ok(orders);
    // }

    @GetMapping("/date")
    public ResponseEntity<List<OrderDTO>> getOrdersByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        List<OrderDTO> orders = orderService.getOrdersByDate(date);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderID}/details")
    public ResponseEntity<List<OrderDetailDTO>> getOrderDetails(@PathVariable Integer orderID) {
        List<OrderDetailDTO> details = orderService.getOrderDetails(orderID);
        return ResponseEntity.ok(details);
    }

    // @PutMapping("/{orderID}/status")
    // public ResponseEntity<OrderDTO> updateOrderStatus(
    // @PathVariable Integer orderID,
    // @RequestParam String status) {
    // OrderDTO updatedOrder = orderService.updateOrderStatus(orderID, status);
    // return ResponseEntity.ok(updatedOrder);
    // }

    @DeleteMapping("/{orderID}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderID) {
        orderService.deleteOrder(orderID);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/revenue")
    public ResponseEntity<Double> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Double totalRevenue = orderService.getTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(totalRevenue);
    }

    // @GetMapping("/revenue/report")
    // public ResponseEntity<Map<String, Object>> getRevenueReport(
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime startDate,
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime endDate) {
    // Map<String, Object> report = orderService.getRevenueReport(startDate,
    // endDate);
    // return ResponseEntity.ok(report);
    // }
}