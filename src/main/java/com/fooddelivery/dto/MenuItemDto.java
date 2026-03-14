package com.fooddelivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MenuItemDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request body to add a new menu item")
    public static class CreateRequest {

        @Schema(description = "Item name", example = "Chicken Biryani", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "Item description", example = "Aromatic basmati rice with tender chicken pieces")
        private String description;

        @Schema(description = "Price in rupees", example = "280.0", requiredMode = Schema.RequiredMode.REQUIRED)
        private Double price;

        @Schema(description = "Category of the item", example = "Main Course",
                allowableValues = {"Starters", "Main Course", "Breads", "Desserts", "Beverages", "Sides", "Pizza", "Burgers", "Pasta"})
        private String category;

        @Schema(description = "Image URL for the item", example = "https://example.com/biryani.jpg")
        private String imageUrl;

        @Schema(description = "Is this a vegetarian item?", example = "false")
        private boolean veg;

        @Schema(description = "Is this item currently available?", example = "true")
        private boolean available;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request body to update a menu item — only pass fields you want to change")
    public static class UpdateRequest {

        @Schema(description = "Updated item name", example = "Chicken Dum Biryani")
        private String name;

        @Schema(description = "Updated description", example = "Slow-cooked dum biryani with saffron rice")
        private String description;

        @Schema(description = "Updated price in rupees", example = "320.0")
        private Double price;

        @Schema(description = "Updated category", example = "Main Course",
                allowableValues = {"Starters", "Main Course", "Breads", "Desserts", "Beverages", "Sides", "Pizza", "Burgers", "Pasta"})
        private String category;

        @Schema(description = "Updated image URL", example = "https://example.com/dum-biryani.jpg")
        private String imageUrl;

        @Schema(description = "Is this a vegetarian item?", example = "false")
        private Boolean veg;

        @Schema(description = "Set to false to mark item as unavailable / out of stock", example = "true")
        private Boolean available;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Menu item details returned in API responses")
    public static class MenuItemResponse {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private String category;
        private String imageUrl;
        private boolean veg;
        private boolean available;
        private Double rating;
        private Long restaurantId;
        private String restaurantName;
    }
}
