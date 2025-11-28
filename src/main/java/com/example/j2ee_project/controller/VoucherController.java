package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.VoucherDTO;
import com.example.j2ee_project.model.request.voucher.VoucherRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.voucher.VoucherServiceInterface;
import com.example.j2ee_project.service.email.EmailServiceInterface;
import com.example.j2ee_project.repository.VoucherRepository;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.entity.Voucher;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vouchers")
@Tag(name = "Voucher Management", description = "APIs for managing restaurant vouchers")
public class VoucherController {

    private final VoucherServiceInterface voucherService;
    private final ResponseHandler responseHandler;
    private final EmailServiceInterface emailService;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;

    @Autowired
    public VoucherController(VoucherServiceInterface voucherService, ResponseHandler responseHandler, EmailServiceInterface emailService, VoucherRepository voucherRepository, UserRepository userRepository) {
        this.voucherService = voucherService;
        this.responseHandler = responseHandler;
        this.emailService = emailService;
        this.voucherRepository = voucherRepository;
        this.userRepository = userRepository;
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

    @PostMapping("/send/all-customers/{voucherCode}")
    public ResponseEntity<?> sendVoucherToAllCustomers(@PathVariable String voucherCode) {
        try {
            Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher với mã: " + voucherCode));

            // Lấy tất cả user có roleId = 1 và email hợp lệ
            List<com.example.j2ee_project.entity.User> users = userRepository.findByRoleId(1);
            if (users == null || users.isEmpty()) {
                return responseHandler.handleNotFound("Không tìm thấy người dùng có roleId = 1");
            }

            // Lọc user có email hợp lệ
            List<Integer> userIds = users.stream()
                    .filter(u -> u.getEmail() != null && !u.getEmail().isEmpty() && isValidEmail(u.getEmail()))
                    .map(u -> u.getUserID())
                    .collect(Collectors.toList());

            if (userIds.isEmpty()) {
                return responseHandler.handleNotFound("Không tìm thấy người dùng có roleId = 1 và email hợp lệ");
            }

            // Gọi service email để gửi thông báo voucher
            emailService.sendVoucherNotificationToUsers(userIds, voucherCode, voucher.getDescription());

            return responseHandler.responseSuccess("Email voucher đã được gửi đến tất cả khách hàng roleId=1 có email hợp lệ", null);
        } catch (Exception e) {
            return responseHandler.handleServerError("Lỗi khi gửi email voucher: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}