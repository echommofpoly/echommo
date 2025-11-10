package com.poly.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull; // Optional validation
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateListingRequest {
    @NotNull
    private Integer itemId;

    @NotNull
    @Positive // Quantity must be greater than 0
    private Integer quantity;

    @NotNull
    @Positive // Price must be greater than 0
    private BigDecimal price;

    @Size(max = 20) // Match DB constraint
    private String listingType = "Player"; // Default to "Player", can be "Admin"
}