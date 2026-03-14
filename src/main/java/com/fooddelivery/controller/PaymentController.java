package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.entity.Payment;
import com.fooddelivery.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Payments", description = "Razorpay, Stripe, and Cash on Delivery payment flows")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/razorpay/initiate/{orderId}")
    @Operation(summary = "Initiate Razorpay payment for an order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiateRazorpay(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Razorpay order created",
                paymentService.initiateRazorpayPayment(orderId)));
    }

    @PostMapping("/razorpay/verify")
    @Operation(summary = "Verify Razorpay payment after frontend callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyRazorpay(
            @RequestParam String gatewayOrderId,
            @RequestParam String paymentId,
            @RequestParam String signature) {
        return ResponseEntity.ok(ApiResponse.success("Payment verification complete",
                paymentService.verifyRazorpayPayment(gatewayOrderId, paymentId, signature)));
    }

    @PostMapping("/stripe/initiate/{orderId}")
    @Operation(summary = "Create a Stripe Payment Intent for an order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiateStripe(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Stripe payment intent created",
                paymentService.initiateStripePayment(orderId)));
    }

    @PostMapping("/stripe/confirm")
    @Operation(summary = "Confirm Stripe payment after frontend stripe.confirmPayment()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmStripe(@RequestParam String paymentIntentId) {
        return ResponseEntity.ok(ApiResponse.success("Stripe payment confirmation complete",
                paymentService.confirmStripePayment(paymentIntentId)));
    }

    @PostMapping("/cod/{orderId}")
    @Operation(summary = "Select Cash on Delivery for an order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cashOnDelivery(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("COD order confirmed",
                paymentService.processCashOnDelivery(orderId)));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment details for an order")
    public ResponseEntity<ApiResponse<Payment>> getPayment(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Payment details",
                paymentService.getPaymentByOrderId(orderId)));
    }
}
