package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.VoucherDTO;
import com.example.j2ee_project.model.request.voucher.VoucherRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.voucher.VoucherServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherServiceInterface voucherService;
    private final ResponseHandler responseHandler;

    @Autowired
    public VoucherController(VoucherServiceInterface voucherService, ResponseHandler responseHandler) {
        this.voucherService = voucherService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createVoucher(@Valid @RequestBody VoucherRequest request) {
        VoucherDTO response = voucherService.createVoucher(request);
        return responseHandler.responseCreated("Tạo voucher thành công", response);
    }

    @GetMapping
    public ResponseEntity<?> getAllVouchers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<VoucherDTO> page = voucherService.getAllVouchers(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách voucher thành công", page);
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getVoucherByCode(@PathVariable String code) {
        VoucherDTO response = voucherService.getVoucherByCode(code);
        return responseHandler.responseSuccess("Lấy thông tin voucher thành công", response);
    }

    @PutMapping("/{code}")
    public ResponseEntity<?> updateVoucher(@PathVariable String code, @Valid @RequestBody VoucherRequest request) {
        VoucherDTO response = voucherService.updateVoucher(code, request);
        return responseHandler.responseSuccess("Cập nhật voucher thành công", response);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<?> deleteVoucher(@PathVariable String code) {
        voucherService.deleteVoucher(code);
        return responseHandler.responseSuccess("Xóa voucher thành công", null);
    }

    @PostMapping("/send/user/{userId}")
    public ResponseEntity<?> sendVoucherToUser(@PathVariable Integer userId, @RequestParam String voucherCode) {
        voucherService.sendVoucherToUser(userId, voucherCode);
        return responseHandler.responseSuccess("Gửi voucher đến user thành công", null);
    }

    @PostMapping("/send/all-customers")
    public ResponseEntity<?> sendVoucherToAllCustomers(@RequestParam String voucherCode) {
        voucherService.sendVoucherToAllCustomers(voucherCode);
        return responseHandler.responseSuccess("Gửi voucher đến tất cả khách hàng thành công", null);
    }
}