package com.poly.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "market_listing")
public class MarketListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id")
    private Integer listingId;

    // EAGER fetch might be acceptable here if you usually need seller/item info
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    @ToString.Exclude // Avoid recursion if User also lists MarketListings
    @EqualsAndHashCode.Exclude
    private User seller;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Ensure quantity is positive via DB check or validation
    @Column(columnDefinition = "INT DEFAULT 1 CHECK (quantity > 0)")
    private Integer quantity = 1;

    // Ensure price is non-negative via DB check or validation
    @Column(nullable = false, precision = 18, scale = 2, columnDefinition = "DECIMAL(18,2) CHECK (price >= 0)")
    private BigDecimal price;

    @Column(name = "listing_type", length = 20) // e.g., Player, Fixed (maybe Auction later?)
    private String listingType;

    @Column(name = "price_type", length = 20, columnDefinition = "NVARCHAR(20) DEFAULT 'Player'") // Player, System, Admin
    private String priceType = "Player";

    @Column(length = 20, columnDefinition = "NVARCHAR(20) DEFAULT 'Active'") // Active, Sold, Cancelled
    private String status = "Active";

    @Column(name = "created_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sold_at") // Timestamp when the item was sold
    private LocalDateTime soldAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Set defaults if they are null, though @ColumnDefault and DB defaults are better
        if (quantity == null) quantity = 1;
        if (status == null) status = "Active";
        if (priceType == null) priceType = "Player";
    }
}