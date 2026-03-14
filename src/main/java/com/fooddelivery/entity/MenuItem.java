package com.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    private String category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_veg")
    private boolean veg;

    @Column(name = "is_available")
    private boolean available = true;

    private Double rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}
