package com.example.j2ee_project.service.customerVoucher;

import com.example.j2ee_project.model.dto.CustomerVoucherDTO;
import com.example.j2ee_project.model.request.customerVoucher.CustomerVoucherRequest;
import org.springframework.data.domain.Page;

public interface CustomerVoucherServiceInterface {
    CustomerVoucherDTO createCustomerVoucher(CustomerVoucherRequest request);

    Page<CustomerVoucherDTO> getAllCustomerVouchers(int offset, int limit, String search);

    CustomerVoucherDTO getCustomerVoucherById(Integer userId, Integer voucherId);

    CustomerVoucherDTO updateCustomerVoucher(Integer userId, Integer voucherId, CustomerVoucherRequest request);

    void deleteCustomerVoucher(Integer userId, Integer voucherId);
}