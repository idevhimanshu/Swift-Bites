package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.dto.RestaurantDto;
import com.fooddelivery.service.FavouriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favourites")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Favourites", description = "Save and manage your favourite restaurants")
public class FavouriteController {

    private final FavouriteService favouriteService;

    public FavouriteController(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @GetMapping
    @Operation(summary = "Get all my favourite restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantDto.RestaurantResponse>>> getFavourites(
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Favourites fetched",
                favouriteService.getFavourites(u.getUsername())));
    }

    @PostMapping("/{restaurantId}/toggle")
    @Operation(summary = "Toggle favourite (add if not saved, remove if already saved)")
    public ResponseEntity<ApiResponse<Map<String, String>>> toggle(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal UserDetails u) {
        String action = favouriteService.toggleFavourite(u.getUsername(), restaurantId);
        return ResponseEntity.ok(ApiResponse.success(
                "added".equals(action) ? "Added to favourites" : "Removed from favourites",
                Map.of("action", action, "restaurantId", String.valueOf(restaurantId))));
    }

    @GetMapping("/{restaurantId}/check")
    @Operation(summary = "Check if a restaurant is in your favourites")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> check(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal UserDetails u) {
        boolean isFav = favouriteService.isFavourite(u.getUsername(), restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Checked",
                Map.of("isFavourite", isFav)));
    }
}
