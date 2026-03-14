package com.fooddelivery.config;

import com.fooddelivery.entity.*;
import com.fooddelivery.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RestaurantRepository restaurantRepository,
                      MenuItemRepository menuItemRepository, CouponRepository couponRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.couponRepository = couponRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;
        log.info("Seeding sample data...");

        // ── Users (@PrePersist forces active=true automatically) ──────────────
        User admin = userRepository.save(User.builder()
                .name("Admin User").email("admin@fooddelivery.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("9000000000").role(User.Role.ADMIN).build());

        User customer = userRepository.save(User.builder()
                .name("Rahul Sharma").email("rahul@example.com")
                .password(passwordEncoder.encode("password123"))
                .phone("9876543210").address("12 MG Road, Bangalore")
                .role(User.Role.CUSTOMER).build());

        User owner = userRepository.save(User.builder()
                .name("Restaurant Owner").email("owner@restaurant.com")
                .password(passwordEncoder.encode("owner123"))
                .phone("9123456789").role(User.Role.RESTAURANT_OWNER).build());

        User deliveryPartner = userRepository.save(User.builder()
                .name("Ravi Kumar").email("ravi@delivery.com")
                .password(passwordEncoder.encode("deliver123"))
                .phone("9555555555").role(User.Role.DELIVERY_PARTNER).build());

        log.info("Users seeded — admin={}, customer={}, owner={}, delivery={}",
                admin.isActive(), customer.isActive(), owner.isActive(), deliveryPartner.isActive());

        // ── Restaurants ───────────────────────────────────────────────────────
        Restaurant r1 = restaurantRepository.save(Restaurant.builder()
                .name("Spice Garden").description("Authentic Indian cuisine")
                .address("45 Brigade Road").city("Bangalore").cuisine("Indian")
                .rating(4.5).ratingCount(120).deliveryTime(35)
                .deliveryFee(30.0).minOrder(150.0)
                .openingTime(LocalTime.of(10, 0)).closingTime(LocalTime.of(23, 0))
                .active(true).owner(owner).build());

        Restaurant r2 = restaurantRepository.save(Restaurant.builder()
                .name("Pizza Palace").description("Wood-fired pizzas and pastas")
                .address("78 Koramangala").city("Bangalore").cuisine("Italian")
                .rating(4.2).ratingCount(85).deliveryTime(45)
                .deliveryFee(50.0).minOrder(200.0)
                .openingTime(LocalTime.of(11, 0)).closingTime(LocalTime.of(22, 30))
                .active(true).owner(owner).build());

        Restaurant r3 = restaurantRepository.save(Restaurant.builder()
                .name("Burger Barn").description("Gourmet burgers and shakes")
                .address("22 Indiranagar").city("Bangalore").cuisine("American")
                .rating(4.0).ratingCount(200).deliveryTime(25)
                .deliveryFee(20.0).minOrder(100.0)
                .openingTime(LocalTime.of(10, 0)).closingTime(LocalTime.of(23, 59))
                .active(true).owner(owner).build());

        // ── Menu Items ────────────────────────────────────────────────────────
        menuItemRepository.saveAll(List.of(
            MenuItem.builder().name("Butter Chicken").description("Creamy tomato curry")
                .price(280.0).category("Main Course").veg(false).available(true).rating(4.7).restaurant(r1).build(),
            MenuItem.builder().name("Paneer Tikka").description("Grilled cottage cheese")
                .price(220.0).category("Starters").veg(true).available(true).rating(4.5).restaurant(r1).build(),
            MenuItem.builder().name("Dal Makhani").description("Slow cooked black lentils")
                .price(180.0).category("Main Course").veg(true).available(true).rating(4.3).restaurant(r1).build(),
            MenuItem.builder().name("Garlic Naan").description("Soft leavened bread")
                .price(50.0).category("Breads").veg(true).available(true).rating(4.6).restaurant(r1).build(),
            MenuItem.builder().name("Margherita Pizza").description("Classic tomato and mozzarella")
                .price(350.0).category("Pizza").veg(true).available(true).rating(4.4).restaurant(r2).build(),
            MenuItem.builder().name("Chicken BBQ Pizza").description("Smoky BBQ chicken")
                .price(420.0).category("Pizza").veg(false).available(true).rating(4.6).restaurant(r2).build(),
            MenuItem.builder().name("Pasta Arrabiata").description("Spicy tomato pasta")
                .price(280.0).category("Pasta").veg(true).available(true).rating(4.1).restaurant(r2).build(),
            MenuItem.builder().name("Classic Cheeseburger").description("Beef patty with cheese")
                .price(199.0).category("Burgers").veg(false).available(true).rating(4.5).restaurant(r3).build(),
            MenuItem.builder().name("Veggie Burger").description("Crispy veg patty")
                .price(149.0).category("Burgers").veg(true).available(true).rating(4.0).restaurant(r3).build(),
            MenuItem.builder().name("Loaded Fries").description("Fries with cheese and jalapenos")
                .price(129.0).category("Sides").veg(true).available(true).rating(4.3).restaurant(r3).build(),
            MenuItem.builder().name("Chocolate Milkshake").description("Thick creamy shake")
                .price(99.0).category("Beverages").veg(true).available(true).rating(4.8).restaurant(r3).build()
        ));

        // ── Coupons ───────────────────────────────────────────────────────────
        couponRepository.saveAll(List.of(
            Coupon.builder().code("WELCOME50").description("50% off up to ₹200 on your first order")
                .discountType(Coupon.DiscountType.PERCENTAGE).discountValue(50.0)
                .minOrderAmount(100.0).maxDiscountAmount(200.0)
                .usageLimit(1000).expiryDate(LocalDate.of(2026, 12, 31)).build(),
            Coupon.builder().code("FLAT100").description("₹100 off on orders above ₹500")
                .discountType(Coupon.DiscountType.FLAT).discountValue(100.0)
                .minOrderAmount(500.0).usageLimit(500)
                .expiryDate(LocalDate.of(2026, 6, 30)).build(),
            Coupon.builder().code("FREEDEL").description("Free delivery — no min order")
                .discountType(Coupon.DiscountType.FLAT).discountValue(50.0)
                .usageLimit(200).expiryDate(LocalDate.of(2026, 3, 31)).build()
        ));

        log.info("=================================================================");
        log.info("Seed complete! Test accounts:");
        log.info("  Admin    -> admin@fooddelivery.com   / admin123");
        log.info("  Customer -> rahul@example.com        / password123");
        log.info("  Owner    -> owner@restaurant.com     / owner123");
        log.info("  Delivery -> ravi@delivery.com        / deliver123");
        log.info("Coupons: WELCOME50 | FLAT100 | FREEDEL");
        log.info("Swagger  -> http://localhost:8080/swagger-ui.html");
        log.info("H2       -> http://localhost:8080/h2-console");
        log.info("=================================================================");
    }
}
