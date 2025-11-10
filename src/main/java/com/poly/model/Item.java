// /EchoMMO/src/main/java/com/poly/model/Item.java
package com.poly.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data // Generates getters, setters, toString, equals, hashCode
@Entity
@Table(name = "item") // Matches the SQL table name
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId; // Matches SQL int

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 20) // e.g., Common, Rare, Epic, Legendary
    private String rarity;

    @Column(name = "base_price", precision = 18, scale = 2, columnDefinition = "DECIMAL(18,2) DEFAULT 0.00")
    private BigDecimal basePrice = BigDecimal.ZERO; // Default value

    @Column(name = "item_type", length = 50) // Broader type
    private String itemType;

    @Column(name = "item_category", length = 50) // Specific type (weapon, armor, potion, resource)
    private String itemCategory;

    @Column(name = "is_tradeable", columnDefinition = "BIT DEFAULT 1")
    private Boolean isTradeable = true; // Default value

    @Column(name = "created_by") // Stores the User ID (int)
    private Integer createdBy; // Matches SQL int

    @Column(name = "created_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()", updatable = false)
    private LocalDateTime createdAt; // Timestamp

    // --- Game Specific Stats ---
    @Column(name = "game_atk", columnDefinition = "INT DEFAULT 0")
    private Integer gameAtk = 0; // Default value

    @Column(name = "game_def", columnDefinition = "INT DEFAULT 0")
    private Integer gameDef = 0; // Default value

    @Column(name = "game_heal", columnDefinition = "INT DEFAULT 0")
    private Integer gameHeal = 0; // Default value (for potions)

    @Column(name = "game_slot", length = 50) // e.g., weapon, armor. Null if not equippable
    private String gameSlot;

    @Column(name = "image_url", length = 255) // Path/URL to item image
    private String imageUrl;

    // Automatically set createdAt timestamp and ensure defaults before persisting
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Redundant checks but safe
        if (basePrice == null) basePrice = BigDecimal.ZERO;
        if (isTradeable == null) isTradeable = true;
        if (gameAtk == null) gameAtk = 0;
        if (gameDef == null) gameDef = 0;
        if (gameHeal == null) gameHeal = 0;
    }
    // No need for manual getters/setters thanks to @Data
}