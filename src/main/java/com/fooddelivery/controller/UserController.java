package com.fooddelivery.controller;

import com.fooddelivery.dto.AddressDto;
import com.fooddelivery.dto.ApiResponse;
import com.fooddelivery.dto.UserDto;
import com.fooddelivery.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Profile", description = "View/update profile, manage addresses, change password")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/profile")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserDto.ProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", userService.getProfile(u.getUsername())));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update my profile",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"name\":\"Rahul Kumar\",\"phone\":\"9999999999\",\"address\":\"45 New Street, Bangalore\"}"
                )
            )
        )
    )
    public ResponseEntity<ApiResponse<UserDto.ProfileResponse>> updateProfile(
            @org.springframework.web.bind.annotation.RequestBody UserDto.UpdateProfileRequest req,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userService.updateProfile(u.getUsername(), req)));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change my password",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"currentPassword\":\"password123\",\"newPassword\":\"newpass456\"}"
                )
            )
        )
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @org.springframework.web.bind.annotation.RequestBody UserDto.ChangePasswordRequest req,
            @AuthenticationPrincipal UserDetails u) {
        userService.changePassword(u.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Password changed", null));
    }

    @GetMapping("/addresses")
    @Operation(summary = "Get all my saved addresses")
    public ResponseEntity<ApiResponse<List<AddressDto.AddressResponse>>> getAddresses(
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Addresses fetched", userService.getAddresses(u.getUsername())));
    }

    @PostMapping("/addresses")
    @Operation(summary = "Add a new address",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"label\":\"Home\",\"fullAddress\":\"123 MG Road, Koramangala, Bangalore - 560034\",\"city\":\"Bangalore\",\"pincode\":\"560034\",\"latitude\":12.9716,\"longitude\":77.5946,\"defaultAddress\":true}"
                )
            )
        )
    )
    public ResponseEntity<ApiResponse<AddressDto.AddressResponse>> addAddress(
            @Valid @org.springframework.web.bind.annotation.RequestBody AddressDto.CreateRequest req,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added", userService.addAddress(u.getUsername(), req)));
    }

    @PutMapping("/addresses/{id}/default")
    @Operation(summary = "Set an address as default")
    public ResponseEntity<ApiResponse<AddressDto.AddressResponse>> setDefault(
            @PathVariable Long id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success("Default address updated",
                userService.setDefaultAddress(u.getUsername(), id)));
    }

    @DeleteMapping("/addresses/{id}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id, @AuthenticationPrincipal UserDetails u) {
        userService.deleteAddress(u.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }
}
