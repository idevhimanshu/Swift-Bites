package com.fooddelivery.controller;

import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.dto.AuthDto;
import com.fooddelivery.service.AuthService;
import com.fooddelivery.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Signup, login, refresh token, and logout")
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    public AuthController(AuthService authService, RateLimitService rateLimitService) {
        this.authService = authService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new user",
        requestBody = @RequestBody(content = @Content(examples = @ExampleObject(value = """
            {"name":"Rahul Sharma","email":"rahul@example.com","password":"password123",
             "phone":"9876543210","address":"12 MG Road, Bangalore","role":"CUSTOMER"}"""))))
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> signup(
            @Valid @org.springframework.web.bind.annotation.RequestBody AuthDto.SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered", authService.signup(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token",
        requestBody = @RequestBody(content = @Content(examples = {
            @ExampleObject(name = "Customer", value = "{\"email\":\"rahul@example.com\",\"password\":\"password123\"}"),
            @ExampleObject(name = "Admin",    value ="{\"email\":\"admin@fooddelivery.com\",\"password\":\"admin123\"}"),
            @ExampleObject(name = "Owner",    value = "{\"email\":\"owner@restaurant.com\",\"password\":\"owner123\"}")
        })))
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> login(
            @Valid @org.springframework.web.bind.annotation.RequestBody AuthDto.LoginRequest request,
            HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        if (!rateLimitService.isAllowed("login:" + ip, true))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many login attempts. Please wait a minute."));
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Get new access token using refresh token",
        requestBody = @RequestBody(content = @Content(examples = @ExampleObject(
            value = "{\"refreshToken\":\"your-refresh-token-here\"}"))))
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> refresh(
            @Valid @org.springframework.web.bind.annotation.RequestBody AuthDto.RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Logout and invalidate refresh token",
        requestBody = @RequestBody(content = @Content(examples = @ExampleObject(
            value = "{\"refreshToken\":\"your-refresh-token-here\"}"))))
    public ResponseEntity<ApiResponse<Void>> logout(
            @org.springframework.web.bind.annotation.RequestBody AuthDto.RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
