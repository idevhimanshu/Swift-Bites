package com.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String address;

    private String city;

    private String cuisine;

    @Column(name = "image_url")
    private String imageUrl;

    private Double rating;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "delivery_time")
    private Integer deliveryTime; // minutes

    @Column(name = "delivery_fee")
    private Double deliveryFee;

    @Column(name = "min_order")
    private Double minOrder;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "is_active")
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<MenuItem> menuItems;
}
