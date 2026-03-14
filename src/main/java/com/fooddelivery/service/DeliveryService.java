package com.fooddelivery.service;

import com.fooddelivery.dto.OrderDto;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.User;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.exception.UnauthorizedException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public DeliveryService(OrderRepository orderRepository, UserRepository userRepository,
                           EmailService emailService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public Page<OrderDto.OrderResponse> getAvailableOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findByStatus(Order.OrderStatus.READY_FOR_PICKUP, pageable);
        return orders.map(this::toOrderResponse);
    }

    public Page<OrderDto.OrderResponse> getMyDeliveries(String email, int page, int size) {
        User partner = findUser(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        return orderRepository.findByDeliveryPartnerId(partner.getId(), pageable).map(this::toOrderResponse);
    }

    @Transactional
    public OrderDto.OrderResponse acceptDelivery(Long orderId, String email) {
        User partner = findUser(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != Order.OrderStatus.READY_FOR_PICKUP)
            throw new BadRequestException("Order is not ready for pickup");
        if (order.getDeliveryPartner() != null)
            throw new BadRequestException("Order already assigned to another delivery partner");
        order.setDeliveryPartner(partner);
        order.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusUpdate(
                saved.getCustomer().getEmail(), saved.getCustomer().getName(), saved);
        return toOrderResponse(saved);
    }

    @Transactional
    public OrderDto.OrderResponse updateDeliveryStatus(Long orderId, Order.OrderStatus newStatus,
                                                        String email) {
        User partner = findUser(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getDeliveryPartner() == null ||
                !order.getDeliveryPartner().getId().equals(partner.getId()))
            throw new UnauthorizedException("This order is not assigned to you");
        if (newStatus != Order.OrderStatus.OUT_FOR_DELIVERY &&
                newStatus != Order.OrderStatus.DELIVERED)
            throw new BadRequestException("Delivery partner can only set OUT_FOR_DELIVERY or DELIVERED");
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusUpdate(
                saved.getCustomer().getEmail(), saved.getCustomer().getName(), saved);
        return toOrderResponse(saved);
    }

    @Transactional
    public void updateLocation(Long orderId, Double lat, Double lon, String email) {
        User partner = findUser(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getDeliveryPartner() == null ||
                !order.getDeliveryPartner().getId().equals(partner.getId()))
            throw new UnauthorizedException("This order is not assigned to you");
        order.setDeliveryLatitude(lat);
        order.setDeliveryLongitude(lon);
        orderRepository.save(order);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private OrderDto.OrderResponse toOrderResponse(Order o) {
        List<OrderDto.OrderItemResponse> items = o.getOrderItems() == null ? List.of() :
                o.getOrderItems().stream().map(i -> OrderDto.OrderItemResponse.builder()
                        .menuItemId(i.getMenuItem().getId())
                        .menuItemName(i.getMenuItem().getName())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .itemTotal(i.getPrice() * i.getQuantity())
                        .build()).collect(Collectors.toList());
        return OrderDto.OrderResponse.builder()
                .id(o.getId()).orderNumber(o.getOrderNumber())
                .customerName(o.getCustomer().getName())
                .restaurantId(o.getRestaurant().getId())
                .restaurantName(o.getRestaurant().getName())
                .deliveryAddress(o.getDeliveryAddress())
                .status(o.getStatus()).items(items)
                .subtotal(o.getSubtotal()).deliveryFee(o.getDeliveryFee())
                .discountAmount(o.getDiscountAmount()).totalAmount(o.getTotalAmount())
                .paymentMethod(o.getPaymentMethod()).paymentStatus(o.getPaymentStatus())
                .specialInstructions(o.getSpecialInstructions())
                .estimatedDeliveryTime(o.getEstimatedDeliveryTime())
                .createdAt(o.getCreatedAt())
                .deliveryPartnerName(o.getDeliveryPartner() != null ? o.getDeliveryPartner().getName() : null)
                .deliveryLatitude(o.getDeliveryLatitude())
                .deliveryLongitude(o.getDeliveryLongitude())
                .build();
    }
}
