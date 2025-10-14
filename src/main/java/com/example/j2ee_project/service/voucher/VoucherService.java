package com.example.j2ee_project.service.voucher;

import com.example.j2ee_project.entity.CustomerVoucher;
import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.entity.Voucher;
import com.example.j2ee_project.exception.DuplicateResourceException;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.VoucherDTO;
import com.example.j2ee_project.model.request.log.LogRequest;
import com.example.j2ee_project.model.request.voucher.VoucherRequest;
import com.example.j2ee_project.repository.CustomerVoucherRepository;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.repository.VoucherRepository;
import com.example.j2ee_project.service.log.LogServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherService implements VoucherServiceInterface {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final CustomerVoucherRepository customerVoucherRepository;
    private final LogServiceInterface logService;

    @Override
    @Transactional
    public VoucherDTO createVoucher(VoucherRequest request) {
        Integer currentUserId = getCurrentUserId();

        String voucherCode = generateVoucherCode();
        if (voucherRepository.existsByVoucherCode(voucherCode)) {
            throw new DuplicateResourceException("Mã voucher đã tồn tại: " + voucherCode);
        }

        Voucher voucher = new Voucher();
        voucher.setVoucherCode(voucherCode);
        voucher.setDescription(request.getDescription());
        voucher.setDiscountPercentage(request.getDiscountPercentage());
        voucher.setApplicableCategory(request.getApplicableCategory());
        voucher.setQuantity(request.getQuantity());
        voucher.setPointsRequired(request.getPointsRequired());

        Voucher saved = voucherRepository.save(voucher);

        logService.createLog(new LogRequest(
                "vouchers",
                saved.getVoucherId(),
                "CREATE",
                "Created voucher: " + saved.getDescription(),
                currentUserId
        ));

        return mapToVoucherDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherDTO> getAllVouchers(int offset, int limit, String search) {
        Integer currentUserId = getCurrentUserId();

        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Voucher> page = voucherRepository.findAll(pageable);

        logService.createLog(new LogRequest(
                "vouchers",
                null,
                "READ",
                "Retrieved list of vouchers with search: " + search,
                currentUserId
        ));

        return page.map(this::mapToVoucherDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherDTO getVoucherByCode(String voucherCode) {
        Integer currentUserId = getCurrentUserId();

        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + voucherCode));

        logService.createLog(new LogRequest(
                "vouchers",
                voucher.getVoucherId(),
                "READ",
                "Retrieved voucher: " + voucher.getDescription(),
                currentUserId
        ));

        return mapToVoucherDTO(voucher);
    }

    @Override
    @Transactional
    public VoucherDTO updateVoucher(String voucherCode, VoucherRequest request) {
        Integer currentUserId = getCurrentUserId();

        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + voucherCode));

        if (request.getDescription() != null) {
            voucher.setDescription(request.getDescription());
        }
        if (request.getDiscountPercentage() != null) {
            voucher.setDiscountPercentage(request.getDiscountPercentage());
        }
        if (request.getApplicableCategory() != null) {
            voucher.setApplicableCategory(request.getApplicableCategory());
        }
        if (request.getQuantity() != null) {
            voucher.setQuantity(request.getQuantity());
        }
        if (request.getPointsRequired() != null) {
            voucher.setPointsRequired(request.getPointsRequired());
        }

        Voucher updated = voucherRepository.save(voucher);

        logService.createLog(new LogRequest(
                "vouchers",
                updated.getVoucherId(),
                "UPDATE",
                "Updated voucher: " + updated.getDescription(),
                currentUserId
        ));

        return mapToVoucherDTO(updated);
    }

    @Override
    @Transactional
    public void deleteVoucher(String voucherCode) {
        Integer currentUserId = getCurrentUserId();

        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + voucherCode));

        Integer voucherId = voucher.getVoucherId();
        voucherRepository.delete(voucher);

        logService.createLog(new LogRequest(
                "vouchers",
                voucherId,
                "DELETE",
                "Deleted voucher with code: " + voucherCode,
                currentUserId
        ));
    }

    @Override
    @Transactional
    public String generateUniqueVoucherForUser(Integer userId) {
        Integer currentUserId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        String voucherCode = generateVoucherCode();
        while (voucherRepository.existsByVoucherCode(voucherCode)) {
            voucherCode = generateVoucherCode();
        }

        Voucher voucher = new Voucher();
        voucher.setVoucherCode(voucherCode);
        voucher.setDescription("Voucher for user ID: " + userId);
        voucher.setDiscountPercentage(10);
        voucher.setQuantity(1);
        voucher.setPointsRequired(0);
        Voucher saved = voucherRepository.save(voucher);

        CustomerVoucher customerVoucher = new CustomerVoucher();
        customerVoucher.setUser(user);
        customerVoucher.setVoucher(voucher);
        customerVoucherRepository.save(customerVoucher);

        logService.createLog(new LogRequest(
                "customer_vouchers",
                saved.getVoucherId(),
                "CREATE",
                "Generated voucher " + voucherCode + " for user ID: " + userId,
                currentUserId
        ));

        return voucherCode;
    }

    @Override
    @Transactional
    public void sendVoucherToUser(Integer userId, String voucherCode) {
        Integer currentUserId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + voucherCode));

        CustomerVoucher customerVoucher = new CustomerVoucher();
        customerVoucher.setUser(user);
        customerVoucher.setVoucher(voucher);
        customerVoucherRepository.save(customerVoucher);

        logService.createLog(new LogRequest(
                "vouchers",
                voucher.getVoucherId(),
                "SEND",
                "Sent voucher " + voucherCode + " to user ID: " + userId,
                currentUserId
        ));
    }

    @Override
    @Transactional
    public void sendVoucherToAllCustomers(String voucherCode) {
        Integer currentUserId = getCurrentUserId();

        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với mã: " + voucherCode));

        List<User> customers = userRepository.findByRoleRoleName("CUSTOMER");
        if (customers.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng nào với vai trò CUSTOMER");
        }

        for (User customer : customers) {
            CustomerVoucher customerVoucher = new CustomerVoucher();
            customerVoucher.setUser(customer);
            customerVoucher.setVoucher(voucher);
            customerVoucherRepository.save(customerVoucher);

            logService.createLog(new LogRequest(
                    "vouchers",
                    voucher.getVoucherId(),
                    "SEND",
                    "Sent voucher " + voucherCode + " to user ID: " + customer.getUserID(),
                    currentUserId
            ));
        }
    }

    private String generateVoucherCode() {
        return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    private VoucherDTO mapToVoucherDTO(Voucher voucher) {
        VoucherDTO dto = new VoucherDTO();
        dto.setVoucherCode(voucher.getVoucherCode());
        dto.setDescription(voucher.getDescription());
        dto.setDiscountPercentage(voucher.getDiscountPercentage());
        dto.setApplicableCategory(voucher.getApplicableCategory());
        dto.setQuantity(voucher.getQuantity());
        dto.setPointsRequired(voucher.getPointsRequired());
        return dto;
    }

    private Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .map(User::getUserID)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));
        }
        return userRepository.findById(1)
                .map(User::getUserID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy admin mặc định"));
    }
}