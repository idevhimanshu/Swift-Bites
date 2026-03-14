package com.fooddelivery.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SwiftBites API")
                .version("1.0.0")
                .description("""
                    SwiftBites - Complete Food Delivery REST API

                    **Test accounts (pre-seeded):**
                    | Role | Email | Password |
                    |------|-------|----------|
                    | Admin | admin@fooddelivery.com | admin123 |
                    | Customer | rahul@example.com | password123 |
                    | Restaurant Owner | owner@restaurant.com | owner123 |
                    | Delivery Partner | ravi@delivery.com | deliver123 |

                    **Available coupons:** `WELCOME50` · `FLAT100` · `FREEDEL`

                    **How to authenticate:** Use POST /api/auth/login → copy `accessToken` → click **Authorize** → enter `Bearer <token>`
                    """)
                .contact(new Contact().name("Food Delivery Team").email("dev@fooddelivery.com")))
            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
            .components(new Components()
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Paste your JWT access token here")))
            .tags(List.of(
                new Tag().name("Auth").description("Signup, login, refresh token, logout"),
                new Tag().name("User Profile").description("Profile, password, saved addresses"),
                new Tag().name("Restaurants").description("Browse and manage restaurants"),
                new Tag().name("Cart").description("Add items and checkout"),
                new Tag().name("Orders").description("Place, track, reorder, cancel"),
                new Tag().name("Payments").description("Razorpay, Stripe, COD"),
                new Tag().name("Coupons").description("Validate and manage discount codes"),
                new Tag().name("Favourites").description("Save favourite restaurants"),
                new Tag().name("Reviews").description("Rate and review restaurants"),
                new Tag().name("Delivery Partner").description("Accept and complete deliveries"),
                new Tag().name("Admin").description("Platform administration")
            ));
    }
}
