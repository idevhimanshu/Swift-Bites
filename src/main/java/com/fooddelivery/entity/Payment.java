package com.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "payment_gateway_id")
    private String paymentGatewayId;   // Razorpay order_id / Stripe payment_intent_id

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(nullable = false)
    private Double amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(name = "gateway_response", length = 1000)
    private String gatewayResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PaymentStatus {
        INITIATED, PENDING, SUCCESS, FAILED, REFUNDED
    }

    public enum PaymentMethod {
        RAZORPAY, STRIPE, CASH_ON_DELIVERY, WALLET
    }
}
