package com.poly.dto;

import jakarta.validation.constraints.NotNull; // Optional validation
import lombok.Data;

@Data
public class BuyItemRequest {
    @NotNull // Ensure listingId is provided
    private Integer listingId;
}