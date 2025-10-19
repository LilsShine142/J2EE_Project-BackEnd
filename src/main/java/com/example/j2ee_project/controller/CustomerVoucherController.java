package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.CustomerVoucherDTO;
import com.example.j2ee_project.model.request.customerVoucher.CustomerVoucherRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.customerVoucher.CustomerVoucherServiceInterface;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customervouchers")
@Tag(name = "Customer Voucher Management", description = "APIs for managing customer vouchers and promotions")
public class CustomerVoucherController {

    private final CustomerVoucherServiceInterface customerVoucherService;
    private final ResponseHandler responseHandler;

    @Autowired
    public CustomerVoucherController(CustomerVoucherServiceInterface customerVoucherService, ResponseHandler responseHandler) {
        this.customerVoucherService = customerVoucherService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCustomerVoucher(@Valid @RequestBody CustomerVoucherRequest request) {
        CustomerVoucherDTO response = customerVoucherService.createCustomerVoucher(request);
        return responseHandler.responseCreated("Tạo voucher của khách hàng thành công", response);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllCustomerVouchers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<CustomerVoucherDTO> page = customerVoucherService.getAllCustomerVouchers(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách voucher của khách hàng thành công", page);
    }

    @GetMapping("/{userId}/{voucherCode}")
    public ResponseEntity<?> getCustomerVoucherById(@PathVariable Integer userId, @PathVariable Integer voucherId) {
        CustomerVoucherDTO response = customerVoucherService.getCustomerVoucherById(userId, voucherId);
        return responseHandler.responseSuccess("Lấy thông tin voucher của khách hàng thành công", response);
    }

    @PutMapping("/{userId}/{voucherCode}")
    public ResponseEntity<?> updateCustomerVoucher(@PathVariable Integer userId, @PathVariable Integer voucherId, @Valid @RequestBody CustomerVoucherRequest request) {
        CustomerVoucherDTO response = customerVoucherService.updateCustomerVoucher(userId, voucherId, request);
        return responseHandler.responseSuccess("Cập nhật voucher của khách hàng thành công", response);
    }

    @DeleteMapping("/{userId}/{voucherCode}")
    public ResponseEntity<?> deleteCustomerVoucher(@PathVariable Integer userId, @PathVariable Integer voucherId) {
        customerVoucherService.deleteCustomerVoucher(userId, voucherId);
        return responseHandler.responseSuccess("Xóa voucher của khách hàng thành công", null);
    }
}