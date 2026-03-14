package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.dto.CouponDto;
import com.fooddelivery.entity.Coupon;
import com.fooddelivery.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@Tag(name = "Coupons", description = "Validate coupons at checkout. Admins can create/manage coupons.")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) { this.couponService = couponService; }

    @PostMapping("/validate")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Validate a coupon and see the discount amount",
        requestBody = @RequestBody(content = @Content(examples = @ExampleObject(
            value = "{\"code\":\"WELCOME50\",\"orderAmount\":350.0}"))))
    public ResponseEntity<ApiResponse<CouponDto.CouponResponse>> validate(
            @Valid @org.springframework.web.bind.annotation.RequestBody CouponDto.ValidateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Coupon is valid", couponService.validateCoupon(req)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "List all coupons (Admin only)")
    public ResponseEntity<ApiResponse<List<CouponDto.CouponResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Coupons fetched", couponService.getAllCoupons()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
        summary = "Create a new coupon (Admin only)",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Percentage discount",
                        value = "{" + "\"code\":\"WELCOME50\"," + "\"description\":\"50% off up to ₹200\"," + "\"discountType\":\"PERCENTAGE\","
                              + "\"discountValue\":50.0,"+ "\"minOrderAmount\":100.0," + "\"maxDiscountAmount\":200.0," + "\"usageLimit\":100," + "\"expiryDate\":\"2026-12-31\"" + "}"
                    ),
                    @ExampleObject(
                        name = "Flat discount",
                        value = "{"
                              + "\"code\":\"FLAT100\","+ "\"description\":\"₹100 off on orders above ₹500\"," + "\"discountType\":\"FLAT\","  + "\"discountValue\":100.0,"
                              + "\"minOrderAmount\":500.0," + "\"expiryDate\":\"2026-06-30\"" + "}"
                    )
                }
            )
        )
    )

    public ResponseEntity<ApiResponse<CouponDto.CouponResponse>> create(
            @Valid @org.springframework.web.bind.annotation.RequestBody CouponDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created", couponService.createCoupon(req)));
    }

    @DeleteMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Deactivate a coupon (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        couponService.deactivateCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deactivated", null));
    }
}
