package com.example.j2ee_project.repository;

import com.example.j2ee_project.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    boolean existsByVoucherCode(String voucherCode);
    Optional<Voucher> findByVoucherCode(String voucherCode);
}