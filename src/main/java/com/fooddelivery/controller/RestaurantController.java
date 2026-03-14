package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.dto.MenuItemDto;
import com.fooddelivery.dto.RestaurantDto;
import com.fooddelivery.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurants", description = "Search, browse, and manage restaurants and menus")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // ── Public endpoints ──────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all active restaurants",
               description = "Returns paginated list of all active restaurants. Sort options: rating, deliveryTime, deliveryFee")
    public ResponseEntity<ApiResponse<Page<RestaurantDto.RestaurantResponse>>> getAllRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy) {
        return ResponseEntity.ok(ApiResponse.success("All restaurants fetched",
                restaurantService.getAllRestaurants(page, size, sortBy)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search restaurants by name, city, or cuisine")
    public ResponseEntity<ApiResponse<Page<RestaurantDto.RestaurantResponse>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String cuisine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy) {
        return ResponseEntity.ok(ApiResponse.success("Restaurants fetched",
                restaurantService.searchRestaurants(query, city, cuisine, page, size, sortBy)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single restaurant by ID")
    public ResponseEntity<ApiResponse<RestaurantDto.RestaurantResponse>> getRestaurant(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Restaurant found",
                restaurantService.getRestaurantById(id)));
    }

    @GetMapping("/{id}/menu")
    @Operation(summary = "Get menu for a restaurant (optional search query)")
    public ResponseEntity<ApiResponse<List<MenuItemDto.MenuItemResponse>>> getMenu(
            @PathVariable Long id,
            @RequestParam(required = false) String query) {
        List<MenuItemDto.MenuItemResponse> items = query != null
                ? restaurantService.searchMenuItems(id, query)
                : restaurantService.getMenuByRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success("Menu fetched", items));
    }

    // ── Owner / Admin endpoints ───────────────────────────────────────────────

    @PostMapping("/manage")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
        summary = "Create a new restaurant",
        description = "Owner is automatically assigned from your JWT token. Only pass the fields below.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = RestaurantDto.CreateRequest.class),
                examples = @ExampleObject(
                    name = "Create Restaurant Example",
                    summary = "Full example with all fields",
                    value = """
                    {
                      "name": "The Biryani House",
                      "description": "Best biryani in town, slow cooked with authentic spices",
                      "address": "123 Main Street, Koramangala",
                      "city": "Bangalore",
                      "cuisine": "Indian",
                      "imageUrl": "https://example.com/biryani.jpg",
                      "deliveryTime": 40,
                      "deliveryFee": 30.0,
                      "minOrder": 150.0,
                      "openingTime": "10:00:00",
                      "closingTime": "23:00:00"
                    }
                    """
                )
            )
        )
    )
    public ResponseEntity<ApiResponse<RestaurantDto.RestaurantResponse>> createRestaurant(
            @org.springframework.web.bind.annotation.RequestBody RestaurantDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Restaurant created",
                restaurantService.createRestaurant(request, userDetails.getUsername())));
    }

    @PutMapping("/manage/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
        summary = "Update restaurant details",
        description = "Only pass the fields you want to update — all fields are optional.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = RestaurantDto.UpdateRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Update name and delivery fee only",
                        summary = "Partial update — only changed fields needed",
                        value = """
                        {
                          "name": "The Biryani House - Koramangala",
                          "deliveryFee": 25.0
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Update timings and min order",
                        summary = "Change opening hours and minimum order",
                        value = """
                        {
                          "openingTime": "09:00:00",
                          "closingTime": "22:00:00",
                          "minOrder": 200.0
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Full update",
                        summary = "Update all fields at once",
                        value = """
                        {
                          "name": "The Biryani House - Koramangala",
                          "description": "Authentic Hyderabadi dum biryani",
                          "address": "456 New Road, Koramangala",
                          "city": "Bangalore",
                          "cuisine": "Indian",
                          "imageUrl": "https://example.com/new-image.jpg",
                          "deliveryTime": 35,
                          "deliveryFee": 25.0,
                          "minOrder": 200.0,
                          "openingTime": "09:00:00",
                          "closingTime": "22:00:00"
                        }
                        """
                    )
                }
            )
        )
    )
    public ResponseEntity<ApiResponse<RestaurantDto.RestaurantResponse>> updateRestaurant(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody RestaurantDto.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated",
                restaurantService.updateRestaurant(id, request, userDetails.getUsername())));
    }

    @PostMapping("/manage/{restaurantId}/menu")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
        summary = "Add a new menu item to restaurant",
        description = "Adds a new food item to the restaurant's menu.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = MenuItemDto.CreateRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Non-veg item",
                        summary = "Add a non-vegetarian dish",
                        value = """
                        {
                          "name": "Chicken Biryani",
                          "description": "Aromatic basmati rice with tender chicken pieces",
                          "price": 280.0,
                          "category": "Main Course",
                          "imageUrl": "https://example.com/chicken-biryani.jpg",
                          "veg": false,
                          "available": true
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Veg item",
                        summary = "Add a vegetarian dish",
                        value = """
                        {
                          "name": "Paneer Tikka",
                          "description": "Grilled cottage cheese with spices",
                          "price": 220.0,
                          "category": "Starters",
                          "imageUrl": "https://example.com/paneer-tikka.jpg",
                          "veg": true,
                          "available": true
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Beverage",
                        summary = "Add a drink",
                        value = """
                        {
                          "name": "Mango Lassi",
                          "description": "Chilled sweet mango yogurt drink",
                          "price": 80.0,
                          "category": "Beverages",
                          "imageUrl": "https://example.com/lassi.jpg",
                          "veg": true,
                          "available": true
                        }
                        """
                    )
                }
            )
        )
    )
    public ResponseEntity<ApiResponse<MenuItemDto.MenuItemResponse>> addMenuItem(
            @PathVariable Long restaurantId,
            @org.springframework.web.bind.annotation.RequestBody MenuItemDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Menu item added",
                restaurantService.addMenuItem(restaurantId, request, userDetails.getUsername())));
    }

    @PutMapping("/manage/{restaurantId}/menu/{itemId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
        summary = "Update a menu item",
        description = "Only pass the fields you want to update — all fields are optional.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = MenuItemDto.UpdateRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Update price only",
                        summary = "Change just the price",
                        value = """
                        {
                          "price": 320.0
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Mark as unavailable",
                        summary = "Temporarily disable an item (out of stock)",
                        value = """
                        {
                          "available": false
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Full update",
                        summary = "Update all fields at once",
                        value = """
                        {
                          "name": "Chicken Dum Biryani",
                          "description": "Slow-cooked dum biryani with saffron rice",
                          "price": 320.0,
                          "category": "Main Course",
                          "imageUrl": "https://example.com/dum-biryani.jpg",
                          "veg": false,
                          "available": true
                        }
                        """
                    )
                }
            )
        )
    )
    public ResponseEntity<ApiResponse<MenuItemDto.MenuItemResponse>> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @org.springframework.web.bind.annotation.RequestBody MenuItemDto.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Menu item updated",
                restaurantService.updateMenuItem(restaurantId, itemId, request, userDetails.getUsername())));
    }

    @DeleteMapping("/manage/{restaurantId}/menu/{itemId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Delete a menu item permanently")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        restaurantService.deleteMenuItem(restaurantId, itemId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted", null));
    }
}
