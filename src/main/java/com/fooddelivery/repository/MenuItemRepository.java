package com.fooddelivery.repository;

import com.fooddelivery.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.category) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<MenuItem> searchMenuItems(@Param("restaurantId") Long restaurantId, @Param("query") String query);

    List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, String category);

    @Query("SELECT m FROM MenuItem m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) AND m.available = true")
    List<MenuItem> searchAllMenuItems(@Param("query") String query);
}
