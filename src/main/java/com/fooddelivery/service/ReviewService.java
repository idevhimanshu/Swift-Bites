package com.fooddelivery.service;

import com.fooddelivery.entity.*;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository, RestaurantRepository restaurantRepository,
                         UserRepository userRepository, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Review addReview(Long restaurantId, Integer rating, String comment,
                            Integer foodRating, Integer deliveryRating,
                            Long orderId, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        if (reviewRepository.existsByCustomerIdAndRestaurantId(customer.getId(), restaurantId))
            throw new BadRequestException("You have already reviewed this restaurant");
        if (rating < 1 || rating > 5)
            throw new BadRequestException("Rating must be between 1 and 5");

        Order order = null;
        if (orderId != null) {
            order = orderRepository.findById(orderId).orElse(null);
            if (order != null && !order.getCustomer().getId().equals(customer.getId()))
                throw new BadRequestException("Order does not belong to this customer");
        }

        Review review = reviewRepository.save(Review.builder()
                .customer(customer).restaurant(restaurant).order(order)
                .rating(rating).comment(comment)
                .foodRating(foodRating).deliveryRating(deliveryRating)
                .build());

        Double avg = reviewRepository.getAverageRating(restaurantId);
        Long count = reviewRepository.getReviewCount(restaurantId);
        restaurant.setRating(avg);
        restaurant.setRatingCount(count.intValue());
        restaurantRepository.save(restaurant);

        return review;
    }

    public List<Review> getReviewsForRestaurant(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
    }

    @Transactional
    public void deleteReview(Long reviewId, String customerEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        if (!review.getCustomer().getEmail().equals(customerEmail))
            throw new BadRequestException("Not authorized to delete this review");
        reviewRepository.delete(review);

        Double avg = reviewRepository.getAverageRating(review.getRestaurant().getId());
        Long count = reviewRepository.getReviewCount(review.getRestaurant().getId());
        Restaurant restaurant = review.getRestaurant();
        restaurant.setRating(avg);
        restaurant.setRatingCount(count.intValue());
        restaurantRepository.save(restaurant);
    }
}
