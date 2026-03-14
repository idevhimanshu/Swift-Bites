package com.fooddelivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

public class RestaurantDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request body to create a new restaurant")
    public static class CreateRequest {

        @Schema(description = "Restaurant name", example = "The Biryani House", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "Short description", example = "Best biryani in town, slow cooked with authentic spices")
        private String description;

        @Schema(description = "Full street address", example = "123 Main Street, Koramangala", requiredMode = Schema.RequiredMode.REQUIRED)
        private String address;

        @Schema(description = "City", example = "Bangalore")
        private String city;

        @Schema(description = "Cuisine type", example = "Indian")
        private String cuisine;

        @Schema(description = "Cover image URL", example = "https://example.com/biryani.jpg")
        private String imageUrl;

        @Schema(description = "Estimated delivery time in minutes", example = "40")
        private Integer deliveryTime;

        @Schema(description = "Delivery fee in rupees", example = "30.0")
        private Double deliveryFee;

        @Schema(description = "Minimum order amount in rupees", example = "150.0")
        private Double minOrder;

        @Schema(description = "Opening time (HH:mm:ss)", example = "10:00:00", type = "string")
        private LocalTime openingTime;

        @Schema(description = "Closing time (HH:mm:ss)", example = "23:00:00", type = "string")
        private LocalTime closingTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request body to update restaurant details — only pass fields you want to change")
    public static class UpdateRequest {

        @Schema(description = "New restaurant name", example = "The Biryani House - Koramangala")
        private String name;

        @Schema(description = "Updated description", example = "Authentic Hyderabadi dum biryani")
        private String description;

        @Schema(description = "Updated address", example = "456 New Road, Koramangala")
        private String address;

        @Schema(description = "City", example = "Bangalore")
        private String city;

        @Schema(description = "Cuisine type", example = "Indian")
        private String cuisine;

        @Schema(description = "Cover image URL", example = "https://example.com/new-image.jpg")
        private String imageUrl;

        @Schema(description = "Estimated delivery time in minutes", example = "35")
        private Integer deliveryTime;

        @Schema(description = "Delivery fee in rupees", example = "25.0")
        private Double deliveryFee;

        @Schema(description = "Minimum order amount in rupees", example = "200.0")
        private Double minOrder;

        @Schema(description = "Opening time (HH:mm:ss)", example = "09:00:00", type = "string")
        private LocalTime openingTime;

        @Schema(description = "Closing time (HH:mm:ss)", example = "22:00:00", type = "string")
        private LocalTime closingTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Restaurant details returned in API responses")
    public static class RestaurantResponse {
        private Long id;
        private String name;
        private String description;
        private String address;
        private String city;
        private String cuisine;
        private String imageUrl;
        private Double rating;
        private Integer ratingCount;
        private Integer deliveryTime;
        private Double deliveryFee;
        private Double minOrder;
        private LocalTime openingTime;
        private LocalTime closingTime;
        private boolean active;
        private String ownerName;
        private String ownerEmail;
    }
}
