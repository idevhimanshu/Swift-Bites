package com.fooddelivery.service;

import com.fooddelivery.dto.OrderDto;
import com.fooddelivery.entity.*;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.exception.UnauthorizedException;
import com.fooddelivery.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CouponService couponService;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository,
                        CouponService couponService, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.couponService = couponService;
        this.emailService = emailService;
    }

    @Transactional
    public OrderDto.OrderResponse placeOrder(OrderDto.PlaceOrderRequest request, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        if (!restaurant.isActive()) throw new BadRequestException("Restaurant is currently unavailable");

        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0;
        for (OrderDto.OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemReq.getMenuItemId()));
            if (!menuItem.isAvailable()) throw new BadRequestException("Menu item unavailable: " + menuItem.getName());
            subtotal += menuItem.getPrice() * itemReq.getQuantity();
            orderItems.add(OrderItem.builder().menuItem(menuItem)
                    .quantity(itemReq.getQuantity()).price(menuItem.getPrice())
                    .customization(itemReq.getCustomization()).build());
        }

        if (restaurant.getMinOrder() != null && subtotal < restaurant.getMinOrder())
            throw new BadRequestException("Minimum order amount is Rs." + restaurant.getMinOrder());

        double deliveryFee = restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee() : 0;
        double discount = couponService.applyAndGetDiscount(request.getCouponCode(), subtotal);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer).restaurant(restaurant)
                .deliveryAddress(request.getDeliveryAddress())
                .subtotal(subtotal).deliveryFee(deliveryFee)
                .discountAmount(discount)
                .totalAmount(subtotal + deliveryFee - discount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .specialInstructions(request.getSpecialInstructions())
                .couponCode(request.getCouponCode())
                .status(Order.OrderStatus.PENDING)
                .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(
                        restaurant.getDeliveryTime() != null ? restaurant.getDeliveryTime() : 45))
                .build();
        order.setOrderItems(orderItems);
        orderItems.forEach(i -> i.setOrder(order));
        Order saved = orderRepository.save(order);
        emailService.sendOrderConfirmation(customer.getEmail(), customer.getName(), saved);
        return toOrderResponse(saved);
    }

    @Transactional
    public OrderDto.OrderResponse reorder(Long orderId, String customerEmail) {
        Order original = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!original.getCustomer().getEmail().equals(customerEmail))
            throw new UnauthorizedException("You can only reorder your own orders");
        OrderDto.PlaceOrderRequest req = OrderDto.PlaceOrderRequest.builder()
                .restaurantId(original.getRestaurant().getId())
                .deliveryAddress(original.getDeliveryAddress())
                .paymentMethod(original.getPaymentMethod())
                .specialInstructions(original.getSpecialInstructions())
                .items(original.getOrderItems().stream().map(i ->
                        OrderDto.OrderItemRequest.builder()
                                .menuItemId(i.getMenuItem().getId())
                                .quantity(i.getQuantity())
                                .customization(i.getCustomization())
                                .build()).collect(Collectors.toList()))
                .build();
        return placeOrder(req, customerEmail);
    }

    public OrderDto.OrderResponse getOrder(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        User user = userRepository.findByEmail(email).orElseThrow();
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isOwner = order.getRestaurant().getOwner().getEmail().equals(email);
        boolean isCustomer = order.getCustomer().getEmail().equals(email);
        boolean isPartner = order.getDeliveryPartner() != null && order.getDeliveryPartner().getEmail().equals(email);
        if (!isAdmin && !isOwner && !isCustomer && !isPartner)
            throw new UnauthorizedException("Access denied");
        return toOrderResponse(order);
    }

    public Page<OrderDto.OrderResponse> getCustomerOrders(String email, int page, int size) {
        User customer = userRepository.findByEmail(email).orElseThrow();
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId(),
                PageRequest.of(page, size)).map(this::toOrderResponse);
    }

    public Page<OrderDto.OrderResponse> getRestaurantOrders(Long restaurantId, int page, int size) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId,
                PageRequest.of(page, size)).map(this::toOrderResponse);
    }

    @Transactional
    public OrderDto.OrderResponse updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusUpdate(
                saved.getCustomer().getEmail(), saved.getCustomer().getName(), saved);
        return toOrderResponse(saved);
    }

    @Transactional
    public OrderDto.OrderResponse cancelOrder(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getCustomer().getEmail().equals(email))
            throw new UnauthorizedException("You can only cancel your own orders");
        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CONFIRMED)
            throw new BadRequestException("Order cannot be cancelled at this stage");
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusUpdate(saved.getCustomer().getEmail(), saved.getCustomer().getName(), saved);
        return toOrderResponse(saved);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    public OrderDto.OrderResponse toOrderResponse(Order o) {
        List<OrderDto.OrderItemResponse> items = o.getOrderItems() == null ? List.of() :
                o.getOrderItems().stream().map(i -> OrderDto.OrderItemResponse.builder()
                        .menuItemId(i.getMenuItem().getId())
                        .menuItemName(i.getMenuItem().getName())
                        .quantity(i.getQuantity()).price(i.getPrice())
                        .itemTotal(i.getPrice() * i.getQuantity()).build())
                .collect(Collectors.toList());
        return OrderDto.OrderResponse.builder()
                .id(o.getId()).orderNumber(o.getOrderNumber())
                .customerName(o.getCustomer().getName())
                .restaurantId(o.getRestaurant().getId())
                .restaurantName(o.getRestaurant().getName())
                .items(items).status(o.getStatus())
                .deliveryAddress(o.getDeliveryAddress())
                .subtotal(o.getSubtotal()).deliveryFee(o.getDeliveryFee())
                .discountAmount(o.getDiscountAmount()).totalAmount(o.getTotalAmount())
                .couponCode(o.getCouponCode())
                .paymentMethod(o.getPaymentMethod()).paymentStatus(o.getPaymentStatus())
                .deliveryPartnerName(o.getDeliveryPartner() != null ? o.getDeliveryPartner().getName() : null)
                .deliveryLatitude(o.getDeliveryLatitude()).deliveryLongitude(o.getDeliveryLongitude())
                .specialInstructions(o.getSpecialInstructions())
                .estimatedDeliveryTime(o.getEstimatedDeliveryTime())
                .createdAt(o.getCreatedAt()).build();
    }
}
