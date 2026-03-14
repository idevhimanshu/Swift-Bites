package com.fooddelivery.dto;
import com.fooddelivery.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDto {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Signup request")
    public static class SignupRequest {
        @NotBlank @Schema(example = "Rahul Sharma") private String name;
        @NotBlank @Email @Schema(example = "rahul@example.com") private String email;
        @NotBlank @Size(min=6) @Schema(example = "password123") private String password;
        @Schema(example = "9876543210") private String phone;
        @Schema(example = "12 MG Road, Bangalore") private String address;
        @Schema(example = "CUSTOMER") private User.Role role;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Login request")
    public static class LoginRequest {
        @NotBlank @Email @Schema(example = "rahul@example.com") private String email;
        @NotBlank @Schema(example = "password123") private String password;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Refresh token request")
    public static class RefreshTokenRequest {
        @NotBlank @Schema(example = "your-refresh-token-here") private String refreshToken;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long userId;
        private String name;
        private String email;
        private String role;
    }
}
