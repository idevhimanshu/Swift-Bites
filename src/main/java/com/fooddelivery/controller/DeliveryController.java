package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.dto.OrderDto;
import com.fooddelivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Delivery Partner", description = "Pick up orders and manage deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/available-orders")
    @Operation(summary = "Get orders ready for pickup in your city")
    public ResponseEntity<ApiResponse<Page<OrderDto.OrderResponse>>> getAvailableOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Available orders",
                deliveryService.getAvailableOrders(page, size)));
    }

    @GetMapping("/my-deliveries")
    @Operation(summary = "Get all deliveries assigned to you")
    public ResponseEntity<ApiResponse<Page<OrderDto.OrderResponse>>> getMyDeliveries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("My deliveries",
                deliveryService.getMyDeliveries(userDetails.getUsername(), page, size)));
    }

    @PostMapping("/orders/{orderId}/accept")
    @Operation(summary = "Accept a delivery order")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> acceptDelivery(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Delivery accepted",
                deliveryService.acceptDelivery(orderId, userDetails.getUsername())));
    }

    @PatchMapping("/orders/{orderId}/status")
    @Operation(summary = "Update delivery status (OUT_FOR_DELIVERY or DELIVERED)",
        requestBody = @RequestBody(content = @Content(examples = {
            @ExampleObject(name = "Picked up", value = """
                {"status":"OUT_FOR_DELIVERY"}"""),
            @ExampleObject(name = "Delivered", value = """
                {"status":"DELIVERED"}""")
        })))
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> updateDeliveryStatus(
            @PathVariable Long orderId,
            @org.springframework.web.bind.annotation.RequestBody OrderDto.UpdateStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Delivery status updated",
                deliveryService.updateDeliveryStatus(orderId, request.getStatus(), userDetails.getUsername())));
    }

    @PatchMapping("/orders/{orderId}/location")
    @Operation(summary = "Update your live delivery location",
        requestBody = @RequestBody(content = @Content(examples =
            @ExampleObject(value = """
                {"latitude":12.9716,"longitude":77.5946}"""))))
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @PathVariable Long orderId,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Double> location,
            @AuthenticationPrincipal UserDetails userDetails) {
        deliveryService.updateLocation(orderId, location.get("latitude"),
                location.get("longitude"), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Location updated", null));
    }
}
