package com.fooddelivery.dto;
import com.fooddelivery.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

public class UserDto {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Update your profile")
    public static class UpdateProfileRequest {
        @Schema(example = "Rahul Sharma") private String name;
        @Schema(example = "9876543210") private String phone;
        @Schema(example = "12 MG Road, Bangalore") private String address;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Change password")
    public static class ChangePasswordRequest {
        @Schema(example = "oldpass123") private String currentPassword;
        @Schema(example = "newpass456") private String newPassword;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfileResponse {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private User.Role role;
        private boolean active;
    }
}
