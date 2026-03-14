package com.fooddelivery.service;

import com.fooddelivery.dto.RestaurantDto;
import com.fooddelivery.entity.Favourite;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.FavouriteRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public FavouriteService(FavouriteRepository favouriteRepository,
                            UserRepository userRepository,
                            RestaurantRepository restaurantRepository) {
        this.favouriteRepository = favouriteRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public List<RestaurantDto.RestaurantResponse> getFavourites(String email) {
        User user = findUser(email);
        return favouriteRepository.findByUserId(user.getId()).stream()
                .map(f -> toResponse(f.getRestaurant()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavourite(String email, Long restaurantId) {
        User user = findUser(email);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
        if (favouriteRepository.existsByUserIdAndRestaurantId(user.getId(), restaurantId))
            throw new BadRequestException("Restaurant already in favourites");
        favouriteRepository.save(Favourite.builder().user(user).restaurant(restaurant).build());
    }
    
    @Transactional
    public String toggleFavourite(String email, Long restaurantId) {
        User user = findUser(email);
        if (favouriteRepository.existsByUserIdAndRestaurantId(user.getId(), restaurantId)) {
            favouriteRepository.deleteByUserIdAndRestaurantId(user.getId(), restaurantId);
            return "removed";
        } else {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
            favouriteRepository.save(Favourite.builder().user(user).restaurant(restaurant).build());
            return "added";
        }
    }
    
    @Transactional
    public void removeFavourite(String email, Long restaurantId) {
        User user = findUser(email);
        if (!favouriteRepository.existsByUserIdAndRestaurantId(user.getId(), restaurantId))
            throw new ResourceNotFoundException("Restaurant not in favourites");
        favouriteRepository.deleteByUserIdAndRestaurantId(user.getId(), restaurantId);
    }

    public boolean isFavourite(String email, Long restaurantId) {
        User user = findUser(email);
        return favouriteRepository.existsByUserIdAndRestaurantId(user.getId(), restaurantId);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private RestaurantDto.RestaurantResponse toResponse(Restaurant r) {
        return RestaurantDto.RestaurantResponse.builder()
                .id(r.getId()).name(r.getName()).description(r.getDescription())
                .address(r.getAddress()).city(r.getCity()).cuisine(r.getCuisine())
                .imageUrl(r.getImageUrl()).rating(r.getRating()).ratingCount(r.getRatingCount())
                .deliveryTime(r.getDeliveryTime()).deliveryFee(r.getDeliveryFee())
                .minOrder(r.getMinOrder()).openingTime(r.getOpeningTime())
                .closingTime(r.getClosingTime()).active(r.isActive())
                .ownerName(r.getOwner() != null ? r.getOwner().getName() : null)
                .ownerEmail(r.getOwner() != null ? r.getOwner().getEmail() : null)
                .build();
    }
}
