package com.example.j2ee_project.controller;

import com.example.j2ee_project.exception.ForbiddenException;
import com.example.j2ee_project.model.dto.OrderDTO;
import com.example.j2ee_project.model.request.order.OrderRequestDTO;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.order.OrderService;
import com.example.j2ee_project.utils._enum.EPermission;
import com.example.j2ee_project.utils.role_permission.RolePermissionUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for managing restaurant orders")
public class OrderController {
    private final OrderService orderService;
    private final ResponseHandler responseHandler;
    private final RolePermissionUtils rolePermissionUtils;

    @Autowired
    public OrderController(OrderService orderService, ResponseHandler responseHandler,
                         RolePermissionUtils rolePermissionUtils) {
        this.orderService = orderService;
        this.responseHandler = responseHandler;
        this.rolePermissionUtils = rolePermissionUtils;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.CREATE_ORDER.getCode())) {
            throw new ForbiddenException("Bạn không có quyền tạo đơn hàng!");
        }
        OrderDTO response = orderService.createOrder(orderRequestDTO);
        return responseHandler.responseCreated("Tạo đơn hàng thành công", response);
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer tableId) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.VIEW_ORDER.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem danh sách đơn hàng!");
        }
        Page<OrderDTO> orderPage = orderService.getAllOrders(offset, limit, search, statusId, userId, tableId);
        return responseHandler.responseSuccess("Lấy danh sách đơn hàng thành công", orderPage);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer orderId) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.VIEW_ORDER.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem thông tin đơn hàng!");
        }
        OrderDTO response = orderService.getOrderById(orderId);
        return responseHandler.responseSuccess("Lấy thông tin đơn hàng thành công", response);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer orderId,
            @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.UPDATE_ORDER.getCode())) {
            throw new ForbiddenException("Bạn không có quyền cập nhật đơn hàng!");
        }
        OrderDTO response = orderService.updateOrder(orderId, orderRequestDTO);
        return responseHandler.responseSuccess("Cập nhật đơn hàng thành công", response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer orderId) {
        if (!rolePermissionUtils.hasPermission(token, EPermission.DELETE_ORDER.getCode())) {
            throw new ForbiddenException("Bạn không có quyền hủy đơn hàng!");
        }
        orderService.deleteOrder(orderId);
        return responseHandler.responseSuccess("Hủy đơn hàng thành công", null);
    }
}