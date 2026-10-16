package com.bitehub.repository;

import com.bitehub.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    Optional<Coupon> findByCodeIgnoreCaseAndIsActiveTrue(String code);
    boolean existsByCodeIgnoreCase(String code);
}
