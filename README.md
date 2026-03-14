# 🍔 Food Delivery REST API

A production-ready Spring Boot REST API for a food delivery application with JWT authentication, role-based access control, payment gateway integration, order tracking, and reviews.

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run the application
```bash
cd food-delivery-api
mvn spring-boot:run
```

The app starts on **http://localhost:8080** with an H2 in-memory database.

---

## 🔗 Key URLs

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | **Swagger UI** — interactive API docs |
| `http://localhost:8080/h2-console` | **H2 Console** — browse the in-memory DB |
| `http://localhost:8080/api-docs` | Raw OpenAPI JSON |

### H2 Console settings
- **JDBC URL:** `jdbc:h2:mem:food_delivery_db`
- **User:** `sa` | **Password:** *(leave blank)*

---

## 👤 Seeded Test Accounts

| Role              | Email                        | Password      |
|-------------------|------------------------------|---------------|
| Admin             | admin@fooddelivery.com       | admin123      |
| Customer          | rahul@example.com            | password123   |
| Restaurant Owner  | owner@restaurant.com         | owner123      |

---

## 📦 API Modules

### 1. Authentication `/api/auth`
| Method | Endpoint          | Description          | Auth |
|--------|-------------------|----------------------|------|
| POST   | `/auth/signup`    | Register new user    | ❌   |
| POST   | `/auth/login`     | Login → JWT token    | ❌   |

**Signup roles:** `CUSTOMER`, `RESTAURANT_OWNER`, `DELIVERY_PARTNER`, `ADMIN`

---

### 2. Restaurants `/api/restaurants`
| Method | Endpoint                              | Description                  | Auth  |
|--------|---------------------------------------|------------------------------|-------|
| GET    | `/restaurants/search`                 | Search by name/city/cuisine  | ❌    |
| GET    | `/restaurants/{id}`                   | Get restaurant details       | ❌    |
| GET    | `/restaurants/{id}/menu`              | Browse menu (with search)    | ❌    |
| POST   | `/restaurants/manage`                 | Create restaurant            | Owner |
| PUT    | `/restaurants/manage/{id}`            | Update restaurant            | Owner |
| POST   | `/restaurants/manage/{id}/menu`       | Add menu item                | Owner |
| PUT    | `/restaurants/manage/{id}/menu/{itemId}` | Update menu item          | Owner |
| DELETE | `/restaurants/manage/{id}/menu/{itemId}` | Delete menu item          | Owner |

**Search params:** `query`, `city`, `cuisine`, `page`, `size`, `sortBy`

---

### 3. Orders `/api/orders`
| Method | Endpoint                        | Description                    | Auth     |
|--------|---------------------------------|--------------------------------|----------|
| POST   | `/orders`                       | Place a new order              | Customer |
| GET    | `/orders/{id}`                  | Get order details              | Auth     |
| GET    | `/orders/my-orders`             | My order history (paginated)   | Customer |
| GET    | `/orders/restaurant/{id}`       | Restaurant's orders            | Owner    |
| PATCH  | `/orders/{id}/status`           | Update order status            | Owner    |
| POST   | `/orders/{id}/cancel`           | Cancel an order                | Customer |

**Order status flow:**
```
PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
                ↘ CANCELLED (from PENDING or CONFIRMED)
```

---

### 4. Payments `/api/payments`

#### Razorpay Flow
```
1. POST /payments/razorpay/initiate/{orderId}  → get gatewayOrderId + keyId
2. Frontend: use Razorpay checkout.js with returned data
3. POST /payments/razorpay/verify?gatewayOrderId=&paymentId=&signature=
```

#### Stripe Flow
```
1. POST /payments/stripe/initiate/{orderId}  → get clientSecret
2. Frontend: use stripe.confirmPayment() with clientSecret
3. POST /payments/stripe/confirm?paymentIntentId=
```

#### Cash on Delivery
```
POST /payments/cod/{orderId}  → immediately confirms order
```

| Method | Endpoint                            | Description                  |
|--------|-------------------------------------|------------------------------|
| POST   | `/payments/razorpay/initiate/{id}`  | Create Razorpay order        |
| POST   | `/payments/razorpay/verify`         | Verify payment signature     |
| POST   | `/payments/stripe/initiate/{id}`    | Create Stripe payment intent |
| POST   | `/payments/stripe/confirm`          | Confirm Stripe payment       |
| POST   | `/payments/cod/{id}`                | Cash on delivery             |
| GET    | `/payments/order/{id}`              | Payment status for order     |

---

### 5. Reviews `/api/reviews`
| Method | Endpoint                          | Description               | Auth     |
|--------|-----------------------------------|---------------------------|----------|
| POST   | `/reviews/restaurant/{id}`        | Add review (1-5 stars)    | Customer |
| GET    | `/reviews/restaurant/{id}`        | Get all reviews           | ❌       |
| DELETE | `/reviews/{reviewId}`             | Delete your review        | Customer |

---

### 6. Admin `/api/admin`
| Method | Endpoint                        | Description                  | Auth  |
|--------|---------------------------------|------------------------------|-------|
| GET    | `/admin/dashboard`              | Stats: users, orders, etc.   | Admin |
| GET    | `/admin/users`                  | All users (paginated)        | Admin |
| DELETE | `/admin/users/{id}`             | Deactivate user              | Admin |
| GET    | `/admin/restaurants`            | All restaurants              | Admin |
| PATCH  | `/admin/restaurants/{id}/toggle`| Activate/deactivate          | Admin |
| GET    | `/admin/orders`                 | All orders                   | Admin |

---

## 🔐 Authentication

All protected endpoints require:
```
Authorization: Bearer <your_jwt_token>
```

Get token from `POST /api/auth/login` response → `data.accessToken`.

In Swagger UI: click **Authorize** → paste your token.

---

## 🗄️ Project Structure
```
src/main/java/com/fooddelivery/
├── config/
│   ├── DataSeeder.java        # Seeds test data on startup
│   ├── OpenApiConfig.java     # Swagger config
│   └── SecurityConfig.java    # Spring Security + JWT
├── controller/
│   ├── AuthController.java
│   ├── RestaurantController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── ReviewController.java
│   └── AdminController.java
├── dto/
│   ├── ApiResponse.java       # Unified response wrapper
│   ├── AuthDto.java
│   └── OrderDto.java
├── entity/
│   ├── User.java
│   ├── Restaurant.java
│   ├── MenuItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   └── Review.java
├── exception/
│   └── GlobalExceptionHandler.java
├── repository/               # Spring Data JPA interfaces
├── security/
│   ├── JwtUtil.java
│   └── JwtAuthenticationFilter.java
└── service/
    ├── AuthService.java
    ├── RestaurantService.java
    ├── OrderService.java
    ├── PaymentService.java
    └── ReviewService.java
```

---

## 💳 Production Payment Setup

### Razorpay
1. Add to `pom.xml`: `com.razorpay:razorpay-java:1.4.3`
2. Set keys in `application.properties`
3. Uncomment real SDK calls in `PaymentService.initiateRazorpayPayment()`

### Stripe
1. Add to `pom.xml`: `com.stripe:stripe-java:24.3.0`
2. Set `payment.stripe.secret-key` in properties
3. Uncomment real SDK calls in `PaymentService.initiateStripePayment()`

---

## 🗃️ Switch to MySQL (Production)
Replace in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/food_delivery_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```
And add MySQL dependency to `pom.xml`:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 🧪 Tests
```bash
mvn test
```
