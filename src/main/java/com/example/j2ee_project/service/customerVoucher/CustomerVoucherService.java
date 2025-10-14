package com.example.j2ee_project.service.customerVoucher;

import com.example.j2ee_project.entity.CustomerVoucher;
import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.entity.Voucher;
import com.example.j2ee_project.entity.keys.KeyCustomerVoucherId;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.CustomerVoucherDTO;
import com.example.j2ee_project.model.request.customerVoucher.CustomerVoucherRequest;
import com.example.j2ee_project.repository.CustomerVoucherRepository;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerVoucherService implements CustomerVoucherServiceInterface {

    private final CustomerVoucherRepository customerVoucherRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;

    @Override
    @Transactional
    public CustomerVoucherDTO createCustomerVoucher(CustomerVoucherRequest request) {
        User user = userRepository.findById(request.getUserID())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserID()));
        Voucher voucher = voucherRepository.findByVoucherCode(request.getVoucherCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + request.getVoucherCode()));

        CustomerVoucher customerVoucher = new CustomerVoucher();
        customerVoucher.setVoucher(voucher);
        customerVoucher.setUser(user);
        customerVoucher.setStatus(request.getStatus());
        customerVoucher.setReceivedDate(LocalDateTime.now());

        CustomerVoucher saved = customerVoucherRepository.save(customerVoucher);
        return mapToCustomerVoucherDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerVoucherDTO> getAllCustomerVouchers(int offset, int limit, String search) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<CustomerVoucher> page = customerVoucherRepository.findByUserFullNameContainingIgnoreCaseOrVoucherDescriptionContainingIgnoreCase(search, search, pageable);
        return page.map(this::mapToCustomerVoucherDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerVoucherDTO getCustomerVoucherById(Integer userId, Integer voucherId) {
        KeyCustomerVoucherId id = new KeyCustomerVoucherId(userId, voucherId);
        CustomerVoucher customerVoucher = customerVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher của khách hàng với userID: " + userId + " và voucherId: " + voucherId));
        return mapToCustomerVoucherDTO(customerVoucher);
    }

    @Override
    @Transactional
    public CustomerVoucherDTO updateCustomerVoucher(Integer userId, Integer voucherId, CustomerVoucherRequest request) {
        KeyCustomerVoucherId id = new KeyCustomerVoucherId(userId, voucherId);
        CustomerVoucher customerVoucher = customerVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher của khách hàng với userID: " + userId + " và voucherId: " + voucherId));

        User user = userRepository.findById(request.getUserID())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserID()));
        Voucher voucher = voucherRepository.findByVoucherCode(request.getVoucherCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + request.getVoucherCode()));

        customerVoucher.setUser(user);
        customerVoucher.setVoucher(voucher);
        customerVoucher.setStatus(request.getStatus());
        customerVoucher.setReceivedDate(LocalDateTime.now());

        CustomerVoucher updated = customerVoucherRepository.save(customerVoucher);
        return mapToCustomerVoucherDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCustomerVoucher(Integer userId, Integer voucherId) {
        KeyCustomerVoucherId id = new KeyCustomerVoucherId(userId, voucherId);
        CustomerVoucher customerVoucher = customerVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher của khách hàng với userID: " + userId + " và voucherId: " + voucherId));
        customerVoucherRepository.delete(customerVoucher);
    }

    private CustomerVoucherDTO mapToCustomerVoucherDTO(CustomerVoucher customerVoucher) {
        CustomerVoucherDTO dto = new CustomerVoucherDTO();
        dto.setUserID(customerVoucher.getUser().getUserID());
        dto.setUserName(customerVoucher.getUser().getFullName());
        dto.setVoucherCode(customerVoucher.getVoucher().getVoucherCode());
        dto.setVoucherDescription(customerVoucher.getVoucher().getDescription());
        dto.setDiscountPercentage(customerVoucher.getVoucher().getDiscountPercentage());
        dto.setReceivedDate(customerVoucher.getReceivedDate());
        dto.setStatus(customerVoucher.getStatus());
        return dto;
    }
}