package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin", description = "Admin-only dashboard and management APIs")
public class AdminController {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public AdminController(UserRepository userRepository,
                           RestaurantRepository restaurantRepository,
                           OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard summary stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalRestaurants", restaurantRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count());
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats", stats));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Users fetched",
                userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Deactivate a user account")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }

    @GetMapping("/restaurants")
    @Operation(summary = "List all restaurants (paginated)")
    public ResponseEntity<ApiResponse<Page<Restaurant>>> getAllRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Restaurants fetched",
                restaurantRepository.findAll(PageRequest.of(page, size))));
    }

    @PatchMapping("/restaurants/{id}/toggle")
    @Operation(summary = "Activate or deactivate a restaurant")
    public ResponseEntity<ApiResponse<Restaurant>> toggleRestaurant(@PathVariable Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        restaurant.setActive(!restaurant.isActive());
        restaurantRepository.save(restaurant);
        return ResponseEntity.ok(ApiResponse.success(
                "Restaurant " + (restaurant.isActive() ? "activated" : "deactivated"), restaurant));
    }

    @GetMapping("/orders")
    @Operation(summary = "List all orders (paginated)")
    public ResponseEntity<ApiResponse<Page<Order>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Orders fetched",
                orderRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
}
