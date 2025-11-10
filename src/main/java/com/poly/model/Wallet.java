package com.poly.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data // Generates getters, setters, etc.
@Entity
@Table(name = "wallet") // Matches SQL table name
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id") // Maps to wallet_id
    private Integer walletId; // Matches SQL int type

    @JsonIgnore // Prevent sending User info back when fetching Wallet
    @OneToOne(fetch = FetchType.LAZY, optional = false) // Each wallet MUST belong to a user, lazy load User
    @JoinColumn(name = "user_id", nullable = false, unique = true) // Foreign key column, must be unique
    @ToString.Exclude // Prevent recursion in toString()
    @EqualsAndHashCode.Exclude // Prevent recursion in equals/hashCode
    private User user;

    // Use DECIMAL for currency, ensure non-negative via columnDefinition
    @Column(precision = 18, scale = 2, nullable = false,
            columnDefinition = "DECIMAL(18,2) DEFAULT 0.00 CHECK (balance >= 0)")
    private BigDecimal balance = BigDecimal.ZERO; // Default value in Java

    @Column(name = "created_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime updatedAt;

    // Set timestamps before first save
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (balance == null) { // Ensure default if somehow missed
            balance = BigDecimal.ZERO;
        }
    }

    // Update timestamp before updates
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    // No manual getters/setters needed
}