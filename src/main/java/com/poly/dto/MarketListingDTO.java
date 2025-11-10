package com.poly.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketListingDTO {
    private Integer listingId;
    private Integer sellerId;
    private String sellerName;
    private Integer itemId;
    private String itemName;
    private String itemDescription;
    private String itemRarity;
    private String itemType; // General type if used
    private String itemCategory; // Specific category like 'weapon', 'potion'
    private Integer quantity;
    private BigDecimal price;
    private String listingType; // Player, Fixed
    private String priceType;   // Player, System, Admin
    private String status;      // Active, Sold, Cancelled
    private LocalDateTime createdAt;
    private String imageUrl;

    // Game stats (optional, but useful for display)
    private Integer gameAtk;
    private Integer gameDef;
    private Integer gameHeal;
    private String gameSlot; // Added slot
}