package com.fooddelivery.repository;

import com.fooddelivery.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    boolean existsByCustomerIdAndRestaurantId(Long customerId, Long restaurantId);
    Optional<Review> findByCustomerIdAndRestaurantId(Long customerId, Long restaurantId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Double getAverageRating(@Param("restaurantId") Long restaurantId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Long getReviewCount(@Param("restaurantId") Long restaurantId);
}
