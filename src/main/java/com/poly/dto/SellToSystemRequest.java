package com.poly.dto;

import jakarta.validation.constraints.NotNull; // Optional validation
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SellToSystemRequest {
    @NotNull
    private Integer itemId;

    @NotNull
    @Positive // Must sell at least 1
    private Integer quantity;
}