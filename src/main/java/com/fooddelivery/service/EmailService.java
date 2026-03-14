package com.fooddelivery.service;

import com.fooddelivery.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.email.from:noreply@fooddelivery.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(String toEmail, String customerName, Order order) {
        String subject = "Order Confirmed! #" + order.getOrderNumber();
        String body = buildOrderConfirmationEmail(customerName, order);
        send(toEmail, subject, body);
    }

    public void sendOrderStatusUpdate(String toEmail, String customerName, Order order) {
        String subject = "Order Update: " + order.getStatus().name().replace("_", " ") + " | #" + order.getOrderNumber();
        String body = buildStatusUpdateEmail(customerName, order);
        send(toEmail, subject, body);
    }

    public void sendPaymentConfirmation(String toEmail, String customerName, Order order) {
        String subject = "Payment Received | #" + order.getOrderNumber();
        String body = buildPaymentEmail(customerName, order);
        send(toEmail, subject, body);
    }

    public void sendWelcomeEmail(String toEmail, String name) {
        String subject = "Welcome to FoodDelivery!";
        String body = "<h2>Hi " + name + ",</h2><p>Welcome aboard! Browse restaurants and place your first order today.</p>";
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        if (!mailEnabled) {
            log.info("[Email MOCK] To: {} | Subject: {}", to, subject);
            return;
        }
        try {
            var msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildOrderConfirmationEmail(String name, Order order) {
        return "<h2>Hi " + name + ", your order is confirmed!</h2>"
            + "<p><b>Order #:</b> " + order.getOrderNumber() + "</p>"
            + "<p><b>Restaurant:</b> " + order.getRestaurant().getName() + "</p>"
            + "<p><b>Total:</b> ₹" + order.getTotalAmount() + "</p>"
            + "<p><b>Estimated Delivery:</b> " + order.getEstimatedDeliveryTime() + "</p>"
            + "<p>Track your order in the app.</p>";
    }

    private String buildStatusUpdateEmail(String name, Order order) {
        String statusMsg = switch (order.getStatus()) {
            case CONFIRMED        -> "Your order has been confirmed by the restaurant!";
            case PREPARING        -> "The restaurant is now preparing your food!";
            case READY_FOR_PICKUP -> "Your order is ready and being picked up!";
            case OUT_FOR_DELIVERY -> "Your order is out for delivery!";
            case DELIVERED        -> "Your order has been delivered. Enjoy your meal!";
            case CANCELLED        -> "Your order has been cancelled.";
            default               -> "Your order status has been updated.";
        };
        return "<h2>Hi " + name + "</h2><p>" + statusMsg + "</p>"
            + "<p><b>Order #:</b> " + order.getOrderNumber() + "</p>"
            + "<p><b>Status:</b> " + order.getStatus() + "</p>";
    }

    private String buildPaymentEmail(String name, Order order) {
        return "<h2>Payment Confirmed!</h2><p>Hi " + name + ",</p>"
            + "<p>We've received your payment of <b>₹" + order.getTotalAmount() + "</b>"
            + " for order <b>#" + order.getOrderNumber() + "</b>.</p>";
    }
}
