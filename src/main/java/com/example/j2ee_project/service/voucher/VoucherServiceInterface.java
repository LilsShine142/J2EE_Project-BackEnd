package com.example.j2ee_project.service.voucher;

import com.example.j2ee_project.model.dto.VoucherDTO;
import com.example.j2ee_project.model.request.voucher.VoucherRequest;
import org.springframework.data.domain.Page;

public interface VoucherServiceInterface {
    VoucherDTO createVoucher(VoucherRequest request);
    Page<VoucherDTO> getAllVouchers(int offset, int limit, String search);
    VoucherDTO getVoucherByCode(String voucherCode);
    VoucherDTO updateVoucher(String voucherCode, VoucherRequest request);
    void deleteVoucher(String voucherCode);
    String generateUniqueVoucherForUser(Integer userId);
    void sendVoucherToUser(Integer userId, String voucherCode);
    void sendVoucherToAllCustomers(String voucherCode);
}