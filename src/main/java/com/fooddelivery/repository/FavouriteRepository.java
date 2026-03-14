package com.fooddelivery.repository;
import com.fooddelivery.entity.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    List<Favourite> findByUserId(Long userId);
    Optional<Favourite> findByUserIdAndRestaurantId(Long userId, Long restaurantId);
    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);
    void deleteByUserIdAndRestaurantId(Long userId, Long restaurantId);
}
