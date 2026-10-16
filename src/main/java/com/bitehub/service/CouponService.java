package com.bitehub.service;

import com.bitehub.dto.CouponDTO;
import com.bitehub.dto.CouponValidateRequest;
import com.bitehub.dto.CouponValidationResponse;
import com.bitehub.exception.BadRequestException;
import com.bitehub.exception.ResourceNotFoundException;
import com.bitehub.model.Coupon;
import com.bitehub.model.DiscountType;
import com.bitehub.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(CouponDTO::new)
                .collect(Collectors.toList());
    }

    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        return new CouponDTO(coupon);
    }

    public CouponValidationResponse validateCoupon(CouponValidateRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return new CouponValidationResponse(false, "Please provide a coupon code", null, 0.0, request.getSubtotal());
        }

        String code = request.getCode().trim().toUpperCase();
        var couponOpt = couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(code);

        if (couponOpt.isEmpty()) {
            return new CouponValidationResponse(false, "Invalid or inactive coupon code", code, 0.0, request.getSubtotal());
        }

        Coupon coupon = couponOpt.get();

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            return new CouponValidationResponse(false, "This coupon has expired", code, 0.0, request.getSubtotal());
        }

        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            return new CouponValidationResponse(false, "Coupon usage limit has been reached", code, 0.0, request.getSubtotal());
        }

        if (coupon.getMinOrderAmount() != null && request.getSubtotal() < coupon.getMinOrderAmount()) {
            return new CouponValidationResponse(false,
                    "Order minimum subtotal must be at least $" + String.format("%.2f", coupon.getMinOrderAmount()),
                    code, 0.0, request.getSubtotal());
        }

        double discount = 0.0;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = (request.getSubtotal() * coupon.getDiscountValue()) / 100.0;
            if (coupon.getMaxDiscountAmount() != null && discount > coupon.getMaxDiscountAmount()) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else {
            discount = coupon.getDiscountValue();
            if (discount > request.getSubtotal()) {
                discount = request.getSubtotal();
            }
        }

        double newTotal = Math.max(0.0, request.getSubtotal() - discount);
        return new CouponValidationResponse(true, "Coupon applied successfully!", code, Math.round(discount * 100.0) / 100.0, Math.round(newTotal * 100.0) / 100.0);
    }

    @Transactional
    public CouponDTO createCoupon(CouponDTO dto) {
        if (couponRepository.existsByCodeIgnoreCase(dto.getCode().trim())) {
            throw new BadRequestException("Coupon code already exists: " + dto.getCode());
        }

        Coupon coupon = new Coupon();
        coupon.setCode(dto.getCode().trim().toUpperCase());
        coupon.setDiscountType(dto.getDiscountType() != null ? dto.getDiscountType() : DiscountType.PERCENTAGE);
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinOrderAmount(dto.getMinOrderAmount() != null ? dto.getMinOrderAmount() : 0.0);
        coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount() != null ? dto.getMaxDiscountAmount() : 100.0);
        coupon.setExpiryDate(dto.getExpiryDate() != null ? dto.getExpiryDate() : LocalDate.now().plusMonths(3));
        coupon.setActive(dto.isActive());
        coupon.setUsageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : 1000);
        coupon.setTimesUsed(0);

        return new CouponDTO(couponRepository.save(coupon));
    }

    @Transactional
    public CouponDTO toggleActive(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        coupon.setActive(!coupon.isActive());
        return new CouponDTO(couponRepository.save(coupon));
    }

    @Transactional
    public void recordUsage(String code) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(c -> {
            c.setTimesUsed(c.getTimesUsed() + 1);
            couponRepository.save(c);
        });
    }

    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }
}
