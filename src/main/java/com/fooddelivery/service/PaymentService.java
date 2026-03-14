package com.fooddelivery.service;

import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Payment;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${payment.razorpay.key-id:rzp_test_dummy}")
    private String razorpayKeyId;

    @Value("${payment.stripe.secret-key:sk_test_dummy}")
    private String stripeSecretKey;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Map<String, Object> initiateRazorpayPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (paymentRepository.findByOrderId(orderId).isPresent())
            throw new BadRequestException("Payment already initiated for order: " + orderId);

        String gatewayOrderId = "rzp_order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        paymentRepository.save(Payment.builder()
                .order(order).paymentGatewayId(gatewayOrderId)
                .amount(order.getTotalAmount()).currency("INR")
                .status(Payment.PaymentStatus.INITIATED).method(Payment.PaymentMethod.RAZORPAY)
                .build());

        Map<String, Object> response = new HashMap<>();
        response.put("gatewayOrderId", gatewayOrderId);
        response.put("amount", (long)(order.getTotalAmount() * 100));
        response.put("currency", "INR");
        response.put("keyId", razorpayKeyId);
        response.put("orderId", orderId);
        response.put("orderNumber", order.getOrderNumber());
        return response;
    }

    @Transactional
    public Map<String, Object> verifyRazorpayPayment(String gatewayOrderId, String paymentId, String signature) {
        Payment payment = paymentRepository.findByPaymentGatewayId(gatewayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        boolean valid = paymentId != null && !paymentId.isBlank();

        if (valid) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setTransactionId(paymentId);
            Order order = payment.getOrder();
            order.setPaymentStatus("PAID");
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("success", valid);
        result.put("message", valid ? "Payment verified successfully" : "Payment verification failed");
        result.put("transactionId", paymentId);
        result.put("orderStatus", payment.getOrder().getStatus());
        return result;
    }

    @Transactional
    public Map<String, Object> initiateStripePayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (paymentRepository.findByOrderId(orderId).isPresent())
            throw new BadRequestException("Payment already initiated for this order");

        String clientSecret = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24) + "_secret_test";
        String paymentIntentId = clientSecret.split("_secret_")[0];

        paymentRepository.save(Payment.builder()
                .order(order).paymentGatewayId(paymentIntentId)
                .amount(order.getTotalAmount()).currency("INR")
                .status(Payment.PaymentStatus.INITIATED).method(Payment.PaymentMethod.STRIPE)
                .build());

        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", clientSecret);
        response.put("paymentIntentId", paymentIntentId);
        response.put("amount", order.getTotalAmount());
        response.put("currency", "INR");
        response.put("orderId", orderId);
        return response;
    }

    @Transactional
    public Map<String, Object> confirmStripePayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByPaymentGatewayId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId(paymentIntentId);
        Order order = payment.getOrder();
        order.setPaymentStatus("PAID");
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);
        paymentRepository.save(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Payment confirmed");
        result.put("orderStatus", order.getStatus());
        return result;
    }

    @Transactional
    public Map<String, Object> processCashOnDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        paymentRepository.save(Payment.builder()
                .order(order).amount(order.getTotalAmount()).currency("INR")
                .status(Payment.PaymentStatus.PENDING).method(Payment.PaymentMethod.CASH_ON_DELIVERY)
                .build());

        order.setPaymentMethod("CASH_ON_DELIVERY");
        order.setPaymentStatus("PENDING_COD");
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Cash on delivery order confirmed");
        result.put("orderNumber", order.getOrderNumber());
        result.put("totalAmount", order.getTotalAmount());
        return result;
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
    }
}
