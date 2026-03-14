package com.fooddelivery.dto;
import com.fooddelivery.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Place a new order")
    public static class PlaceOrderRequest {
        @NotNull @Schema(example = "1") private Long restaurantId;
        @NotEmpty private List<OrderItemRequest> items;
        @NotBlank @Schema(example = "12 MG Road, Koramangala, Bangalore") private String deliveryAddress;
        @Schema(example = "CARD") private String paymentMethod;
        @Schema(example = "Extra spicy please") private String specialInstructions;
        @Schema(example = "WELCOME50", description = "Optional coupon code") private String couponCode;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull @Schema(example = "1") private Long menuItemId;
        @Min(1) @Schema(example = "2") private int quantity;
        @Schema(example = "Less oil") private String customization;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateStatusRequest {
        @Schema(example = "CONFIRMED") private Order.OrderStatus status;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemResponse {
        private Long menuItemId;
        private String menuItemName;
        private int quantity;
        private Double price;
        private Double itemTotal;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderResponse {
        private Long id;
        private String orderNumber;
        private String customerName;
        private Long restaurantId;
        private String restaurantName;
        private List<OrderItemResponse> items;
        private Order.OrderStatus status;
        private String deliveryAddress;
        private Double subtotal;
        private Double deliveryFee;
        private Double discountAmount;
        private Double totalAmount;
        private String couponCode;
        private String paymentMethod;
        private String paymentStatus;
        private String deliveryPartnerName;
        private Double deliveryLatitude;
        private Double deliveryLongitude;
        private String specialInstructions;
        private LocalDateTime estimatedDeliveryTime;
        private LocalDateTime createdAt;
    }
}
