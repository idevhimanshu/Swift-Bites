package com.fooddelivery.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

public class CartDto {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Add or update an item in the cart")
    public static class AddItemRequest {
        @NotNull @Schema(example = "1", description = "Menu item ID") private Long menuItemId;
        @Min(1) @Schema(example = "2") private int quantity;
        @Schema(example = "Extra spicy") private String customization;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CartItemResponse {
        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private String category;
        private boolean veg;
        private Double price;
        private int quantity;
        private Double itemTotal;
        private String customization;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CartResponse {
        private Long cartId;
        private Long restaurantId;
        private String restaurantName;
        private List<CartItemResponse> items;
        private Double subtotal;
        private Double deliveryFee;
        private Double totalAmount;
        private int itemCount;
    }
}
