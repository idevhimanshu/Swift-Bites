package com.fooddelivery.service;

import com.fooddelivery.dto.CartDto;
import com.fooddelivery.entity.*;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository,
                       MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public CartDto.CartResponse getCart(String email) {
        User user = findUser(email);
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) return emptyCart();
        return toCartResponse(cart);
    }

    @Transactional
    public CartDto.CartResponse addItem(String email, CartDto.AddItemRequest req) {
        User user = findUser(email);
        MenuItem menuItem = menuItemRepository.findById(req.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (!menuItem.isAvailable()) throw new BadRequestException("Item is currently unavailable");

        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() ->
                cartRepository.save(Cart.builder().user(user).build()));

        // If cart has items from a different restaurant, clear it
        if (cart.getRestaurant() != null && !cart.getRestaurant().getId().equals(menuItem.getRestaurant().getId())) {
            cart.getItems().clear();
        }
        cart.setRestaurant(menuItem.getRestaurant());

        // Check if item already in cart
        cart.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(req.getMenuItemId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + req.getQuantity()),
                        () -> cart.getItems().add(CartItem.builder()
                                .cart(cart).menuItem(menuItem)
                                .quantity(req.getQuantity())
                                .customization(req.getCustomization())
                                .build())
                );
        return toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartDto.CartResponse updateItem(String email, Long itemId, int quantity) {
        User user = findUser(email);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart is empty"));
        if (quantity <= 0) {
            cart.getItems().removeIf(i -> i.getId().equals(itemId));
        } else {
            cart.getItems().stream().filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Item not in cart"))
                    .setQuantity(quantity);
        }
        if (cart.getItems().isEmpty()) cart.setRestaurant(null);
        return toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(String email) {
        User user = findUser(email);
        cartRepository.findByUserId(user.getId()).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setRestaurant(null);
            cartRepository.save(cart);
        });
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CartDto.CartResponse emptyCart() {
        return CartDto.CartResponse.builder().items(java.util.List.of())
                .subtotal(0.0).deliveryFee(0.0).totalAmount(0.0).itemCount(0).build();
    }

    private CartDto.CartResponse toCartResponse(Cart cart) {
        var items = cart.getItems().stream().map(i -> CartDto.CartItemResponse.builder()
                .id(i.getId())
                .menuItemId(i.getMenuItem().getId())
                .menuItemName(i.getMenuItem().getName())
                .category(i.getMenuItem().getCategory())
                .veg(i.getMenuItem().isVeg())
                .price(i.getMenuItem().getPrice())
                .quantity(i.getQuantity())
                .itemTotal(i.getMenuItem().getPrice() * i.getQuantity())
                .customization(i.getCustomization())
                .build()).toList();

        double subtotal = items.stream().mapToDouble(CartDto.CartItemResponse::getItemTotal).sum();
        double deliveryFee = cart.getRestaurant() != null && cart.getRestaurant().getDeliveryFee() != null
                ? cart.getRestaurant().getDeliveryFee() : 0;

        return CartDto.CartResponse.builder()
                .cartId(cart.getId())
                .restaurantId(cart.getRestaurant() != null ? cart.getRestaurant().getId() : null)
                .restaurantName(cart.getRestaurant() != null ? cart.getRestaurant().getName() : null)
                .items(items)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .totalAmount(subtotal + deliveryFee)
                .itemCount(items.stream().mapToInt(CartDto.CartItemResponse::getQuantity).sum())
                .build();
    }
}
