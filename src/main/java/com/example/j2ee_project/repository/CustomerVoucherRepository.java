package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.CustomerVoucher;
import com.example.j2ee_project.entity.keys.KeyCustomerVoucherId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, KeyCustomerVoucherId> {
    Page<CustomerVoucher> findByUserFullNameContainingIgnoreCaseOrVoucherDescriptionContainingIgnoreCase(
            String userFullName, String voucherDescription, Pageable pageable);
}