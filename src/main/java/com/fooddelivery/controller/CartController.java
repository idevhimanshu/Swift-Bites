package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.dto.CartDto;
import com.fooddelivery.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Cart", description = "Add items, view cart, and checkout to place an order")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) { this.cartService = cartService; }

    @GetMapping
    @Operation(summary = "View my current cart")
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> getCart(@AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Cart fetched", cartService.getCart(u.getUsername())));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to cart (adding from a different restaurant clears the cart)",
        requestBody = @RequestBody(content = @Content(examples = {
            @ExampleObject(name = "Add item", value = "{\"menuItemId\":1,\"quantity\":2,\"customization\":\"Extra spicy\"}"),
            @ExampleObject(name = "No customization", value = "{\"menuItemId\":3,\"quantity\":1}")
        })))
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> addItem(
            @Valid @org.springframework.web.bind.annotation.RequestBody CartDto.AddItemRequest req,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Item added", cartService.addItem(u.getUsername(), req)));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update quantity of a cart item (set to 0 to remove)")
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> updateItem(
            @PathVariable Long itemId,
            @RequestParam int quantity,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Cart updated",
                cartService.updateItem(u.getUsername(), itemId, quantity)));
    }

    @DeleteMapping
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserDetails u) {
        cartService.clearCart(u.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
