package com.fooddelivery.service;

import com.fooddelivery.dto.CouponDto;
import com.fooddelivery.entity.Coupon;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceAlreadyExistsException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.CouponRepository;
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

    public List<CouponDto.CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(c -> toCouponResponse(c, null)).collect(Collectors.toList());
    }

    @Transactional
    public CouponDto.CouponResponse createCoupon(CouponDto.CreateRequest req) {
        if (couponRepository.existsByCodeIgnoreCase(req.getCode()))
            throw new ResourceAlreadyExistsException("Coupon code already exists: " + req.getCode());
        Coupon coupon = Coupon.builder()
                .code(req.getCode().toUpperCase()).description(req.getDescription())
                .discountType(req.getDiscountType()).discountValue(req.getDiscountValue())
                .minOrderAmount(req.getMinOrderAmount()).maxDiscountAmount(req.getMaxDiscountAmount())
                .usageLimit(req.getUsageLimit()).expiryDate(req.getExpiryDate())
                .build();
        return toCouponResponse(couponRepository.save(coupon), null);
    }

    @Transactional
    public void deactivateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        coupon.setActive(false);
        couponRepository.save(coupon);
    }

    public CouponDto.CouponResponse validateCoupon(CouponDto.ValidateRequest req) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(req.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid coupon code"));
        validateCouponRules(coupon, req.getOrderAmount());
        double discount = calculateDiscount(coupon, req.getOrderAmount());
        return toCouponResponse(coupon, discount);
    }

    public double applyAndGetDiscount(String code, double orderAmount) {
        if (code == null || code.isBlank()) return 0;
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BadRequestException("Invalid coupon code: " + code));
        validateCouponRules(coupon, orderAmount);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
        return calculateDiscount(coupon, orderAmount);
    }

    private void validateCouponRules(Coupon coupon, double amount) {
        if (!coupon.isActive()) throw new BadRequestException("Coupon is no longer active");
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now()))
            throw new BadRequestException("Coupon has expired");
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit())
            throw new BadRequestException("Coupon usage limit reached");
        if (coupon.getMinOrderAmount() != null && amount < coupon.getMinOrderAmount())
            throw new BadRequestException("Minimum order amount for this coupon is ₹" + coupon.getMinOrderAmount());
    }

    private double calculateDiscount(Coupon coupon, double amount) {
        double discount = coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE
                ? amount * coupon.getDiscountValue() / 100
                : coupon.getDiscountValue();
        if (coupon.getMaxDiscountAmount() != null)
            discount = Math.min(discount, coupon.getMaxDiscountAmount());
        return Math.min(discount, amount);
    }

    private CouponDto.CouponResponse toCouponResponse(Coupon c, Double calculatedDiscount) {
        return CouponDto.CouponResponse.builder()
                .id(c.getId()).code(c.getCode()).description(c.getDescription())
                .discountType(c.getDiscountType()).discountValue(c.getDiscountValue())
                .minOrderAmount(c.getMinOrderAmount()).maxDiscountAmount(c.getMaxDiscountAmount())
                .usageLimit(c.getUsageLimit()).usedCount(c.getUsedCount())
                .expiryDate(c.getExpiryDate()).active(c.isActive())
                .calculatedDiscount(calculatedDiscount)
                .build();
    }
}
