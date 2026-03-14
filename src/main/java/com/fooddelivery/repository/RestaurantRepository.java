package com.fooddelivery.repository;

import com.fooddelivery.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.cuisine) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.city) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Restaurant> searchRestaurants(@Param("query") String query, Pageable pageable);

    Page<Restaurant> findByActiveTrueAndCity(String city, Pageable pageable);

    Page<Restaurant> findByActiveTrueAndCuisineIgnoreCase(String cuisine, Pageable pageable);

    List<Restaurant> findByOwnerIdAndActiveTrue(Long ownerId);

    Page<Restaurant> findByActiveTrue(Pageable pageable);
}
