package com.fooddelivery.dto;
import com.fooddelivery.entity.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

public class CouponDto {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Request to create a new coupon")
    public static class CreateRequest {
        @NotBlank @Schema(example = "WELCOME50") private String code;
        @Schema(example = "50% off on your first order") private String description;
        @NotNull @Schema(example = "PERCENTAGE") private Coupon.DiscountType discountType;
        @NotNull @Schema(example = "50.0") private Double discountValue;
        @Schema(example = "100.0") private Double minOrderAmount;
        @Schema(example = "200.0") private Double maxDiscountAmount;
        @Schema(example = "100") private Integer usageLimit;
        @Schema(example = "2025-12-31") private LocalDate expiryDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Validate a coupon before applying")
    public static class ValidateRequest {
        @NotBlank @Schema(example = "WELCOME50") private String code;
        @NotNull @Schema(example = "350.0") private Double orderAmount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CouponResponse {
        private Long id;
        private String code;
        private String description;
        private Coupon.DiscountType discountType;
        private Double discountValue;
        private Double minOrderAmount;
        private Double maxDiscountAmount;
        private Integer usageLimit;
        private Integer usedCount;
        private LocalDate expiryDate;
        private boolean active;
        private Double calculatedDiscount;
    }
}
