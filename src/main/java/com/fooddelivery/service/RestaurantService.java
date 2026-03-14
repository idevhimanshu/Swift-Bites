package com.fooddelivery.service;

import com.fooddelivery.dto.MenuItemDto;
import com.fooddelivery.dto.RestaurantDto;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.exception.UnauthorizedException;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             MenuItemRepository menuItemRepository,
                             UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    // ── Restaurant ────────────────────────────────────────────────────────────

    public Page<RestaurantDto.RestaurantResponse> getAllRestaurants(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return restaurantRepository.findByActiveTrue(pageable).map(this::toRestaurantResponse);
    }

    public Page<RestaurantDto.RestaurantResponse> searchRestaurants(String query, String city,
                                                                     String cuisine, int page,
                                                                     int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Restaurant> results;
        if (query != null && !query.isBlank())
            results = restaurantRepository.searchRestaurants(query, pageable);
        else if (city != null && !city.isBlank())
            results = restaurantRepository.findByActiveTrueAndCity(city, pageable);
        else if (cuisine != null && !cuisine.isBlank())
            results = restaurantRepository.findByActiveTrueAndCuisineIgnoreCase(cuisine, pageable);
        else
            results = restaurantRepository.findAll(pageable);
        return results.map(this::toRestaurantResponse);
    }

    public RestaurantDto.RestaurantResponse getRestaurantById(Long id) {
        return toRestaurantResponse(findById(id));
    }

    @Transactional
    public RestaurantDto.RestaurantResponse createRestaurant(RestaurantDto.CreateRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerEmail));

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .cuisine(request.getCuisine())
                .imageUrl(request.getImageUrl())
                .deliveryTime(request.getDeliveryTime())
                .deliveryFee(request.getDeliveryFee())
                .minOrder(request.getMinOrder())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .active(true)
                .ratingCount(0)
                .owner(owner)
                .build();

        return toRestaurantResponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantDto.RestaurantResponse updateRestaurant(Long id, RestaurantDto.UpdateRequest request, String ownerEmail) {
        Restaurant existing = findById(id);
        validateOwnership(existing, ownerEmail);

        if (request.getName() != null)        existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getAddress() != null)     existing.setAddress(request.getAddress());
        if (request.getCity() != null)        existing.setCity(request.getCity());
        if (request.getCuisine() != null)     existing.setCuisine(request.getCuisine());
        if (request.getImageUrl() != null)    existing.setImageUrl(request.getImageUrl());
        if (request.getDeliveryTime() != null) existing.setDeliveryTime(request.getDeliveryTime());
        if (request.getDeliveryFee() != null)  existing.setDeliveryFee(request.getDeliveryFee());
        if (request.getMinOrder() != null)     existing.setMinOrder(request.getMinOrder());
        if (request.getOpeningTime() != null)  existing.setOpeningTime(request.getOpeningTime());
        if (request.getClosingTime() != null)  existing.setClosingTime(request.getClosingTime());

        return toRestaurantResponse(restaurantRepository.save(existing));
    }

    // ── Menu Items ────────────────────────────────────────────────────────────

    public List<MenuItemDto.MenuItemResponse> getMenuByRestaurant(Long restaurantId) {
        findById(restaurantId);
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId)
                .stream().map(this::toMenuItemResponse).collect(Collectors.toList());
    }

    public List<MenuItemDto.MenuItemResponse> searchMenuItems(Long restaurantId, String query) {
        return menuItemRepository.searchMenuItems(restaurantId, query)
                .stream().map(this::toMenuItemResponse).collect(Collectors.toList());
    }

    @Transactional
    public MenuItemDto.MenuItemResponse addMenuItem(Long restaurantId,
                                                    MenuItemDto.CreateRequest request,
                                                    String ownerEmail) {
        Restaurant restaurant = findById(restaurantId);
        validateOwnership(restaurant, ownerEmail);

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .veg(request.isVeg())
                .available(request.isAvailable())
                .restaurant(restaurant)
                .build();

        return toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    @Transactional
    public MenuItemDto.MenuItemResponse updateMenuItem(Long restaurantId, Long itemId,
                                                       MenuItemDto.UpdateRequest request,
                                                       String ownerEmail) {
        Restaurant restaurant = findById(restaurantId);
        validateOwnership(restaurant, ownerEmail);

        MenuItem existing = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemId));

        if (request.getName() != null)        existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getPrice() != null)       existing.setPrice(request.getPrice());
        if (request.getCategory() != null)    existing.setCategory(request.getCategory());
        if (request.getImageUrl() != null)    existing.setImageUrl(request.getImageUrl());
        if (request.getAvailable() != null)   existing.setAvailable(request.getAvailable());
        if (request.getVeg() != null)         existing.setVeg(request.getVeg());

        return toMenuItemResponse(menuItemRepository.save(existing));
    }

    @Transactional
    public void deleteMenuItem(Long restaurantId, Long itemId, String ownerEmail) {
        Restaurant restaurant = findById(restaurantId);
        validateOwnership(restaurant, ownerEmail);
        menuItemRepository.deleteById(itemId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Restaurant findById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
    }

    private void validateOwnership(Restaurant restaurant, String ownerEmail) {
        if (!restaurant.getOwner().getEmail().equals(ownerEmail))
            throw new UnauthorizedException("You do not own this restaurant");
    }

    private RestaurantDto.RestaurantResponse toRestaurantResponse(Restaurant r) {
        return RestaurantDto.RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .address(r.getAddress())
                .city(r.getCity())
                .cuisine(r.getCuisine())
                .imageUrl(r.getImageUrl())
                .rating(r.getRating())
                .ratingCount(r.getRatingCount())
                .deliveryTime(r.getDeliveryTime())
                .deliveryFee(r.getDeliveryFee())
                .minOrder(r.getMinOrder())
                .openingTime(r.getOpeningTime())
                .closingTime(r.getClosingTime())
                .active(r.isActive())
                .ownerName(r.getOwner() != null ? r.getOwner().getName() : null)
                .ownerEmail(r.getOwner() != null ? r.getOwner().getEmail() : null)
                .build();
    }

    private MenuItemDto.MenuItemResponse toMenuItemResponse(MenuItem m) {
        return MenuItemDto.MenuItemResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .price(m.getPrice())
                .category(m.getCategory())
                .imageUrl(m.getImageUrl())
                .veg(m.isVeg())
                .available(m.isAvailable())
                .rating(m.getRating())
                .restaurantId(m.getRestaurant().getId())
                .restaurantName(m.getRestaurant().getName())
                .build();
    }
}
