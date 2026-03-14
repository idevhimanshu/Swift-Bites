package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.dto.OrderDto;
import com.fooddelivery.entity.Order;
import com.fooddelivery.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Orders", description = "Place, track, cancel, and reorder")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    @Operation(summary = "Place a new order",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Order with coupon",
                        value = "{\"restaurantId\":1,\"deliveryAddress\":\"12 MG Road, Bangalore\",\"paymentMethod\":\"CARD\",\"specialInstructions\":\"Extra spicy\",\"couponCode\":\"WELCOME50\",\"items\":[{\"menuItemId\":1,\"quantity\":2},{\"menuItemId\":4,\"quantity\":1}]}"
                    ),
                    @ExampleObject(
                        name = "Order without coupon",
                        value = "{\"restaurantId\":2,\"deliveryAddress\":\"45 Koramangala, Bangalore\",\"paymentMethod\":\"COD\",\"items\":[{\"menuItemId\":5,\"quantity\":1}]}"
                    )
                }
            )
        )
    )
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> placeOrder(
            @Valid @org.springframework.web.bind.annotation.RequestBody OrderDto.PlaceOrderRequest request,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed", orderService.placeOrder(request, u.getUsername())));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details by ID")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> getOrder(
            @PathVariable Long orderId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Order found",
                orderService.getOrder(orderId, u.getUsername())));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get my order history (optional status filter)",
        description = "Filter by status: PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED")
    public ResponseEntity<ApiResponse<Page<OrderDto.OrderResponse>>> myOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Orders fetched",
                orderService.getCustomerOrders(u.getUsername(), page, size)));
    }

    @PostMapping("/{orderId}/reorder")
    @Operation(summary = "Reorder a previous order with the same items")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> reorder(
            @PathVariable Long orderId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reorder placed", orderService.reorder(orderId, u.getUsername())));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order (only PENDING or CONFIRMED)")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> cancel(
            @PathVariable Long orderId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                orderService.cancelOrder(orderId, u.getUsername())));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','ADMIN')")
    @Operation(summary = "Update order status (restaurant owner / admin)",
        description = "Valid transitions: PENDING→CONFIRMED→PREPARING→READY_FOR_PICKUP→OUT_FOR_DELIVERY→DELIVERED")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> updateStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                orderService.updateOrderStatus(orderId, status)));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','ADMIN')")
    @Operation(summary = "Get all orders for a restaurant")
    public ResponseEntity<ApiResponse<Page<OrderDto.OrderResponse>>> restaurantOrders(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Restaurant orders fetched",
                orderService.getRestaurantOrders(restaurantId, page, size)));
    }
}
