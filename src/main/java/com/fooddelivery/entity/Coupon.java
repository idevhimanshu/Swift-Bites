package com.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;  // PERCENTAGE or FLAT

    @Column(nullable = false)
    private Double discountValue;

    private Double minOrderAmount;
    private Double maxDiscountAmount;

    private Integer usageLimit;
    private Integer usedCount;

    private LocalDate expiryDate;
    private boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (usedCount == null) usedCount = 0;
        active = true;
    }

    public enum DiscountType { PERCENTAGE, FLAT }
}
