package com.fooddelivery.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class AddressDto {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Request to add a new address")
    public static class CreateRequest {
        @NotBlank @Schema(example = "Home") private String label;
        @NotBlank @Schema(example = "123 MG Road, Koramangala, Bangalore - 560034") private String fullAddress;
        @Schema(example = "Bangalore") private String city;
        @Schema(example = "560034") private String pincode;
        @Schema(example = "12.9716") private Double latitude;
        @Schema(example = "77.5946") private Double longitude;
        @Schema(example = "true") private boolean defaultAddress;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AddressResponse {
        private Long id;
        private String label;
        private String fullAddress;
        private String city;
        private String pincode;
        private Double latitude;
        private Double longitude;
        private boolean defaultAddress;
    }
}
