package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.entity.Review;
import com.fooddelivery.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Reviews", description = "Rate and review restaurants")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Add a review for a restaurant")
    public ResponseEntity<ApiResponse<Review>> addReview(
            @PathVariable Long restaurantId,
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Review review = reviewService.addReview(restaurantId, request.getRating(), request.getComment(),
                request.getFoodRating(), request.getDeliveryRating(),
                request.getOrderId(), userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Review added", review));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get all reviews for a restaurant")
    public ResponseEntity<ApiResponse<List<Review>>> getReviews(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.success("Reviews fetched",
                reviewService.getReviewsForRestaurant(restaurantId)));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete your review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId, @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }

    @Data
    public static class ReviewRequest {
        private Integer rating;
        private String comment;
        private Integer foodRating;
        private Integer deliveryRating;
        private Long orderId;
    }
}
