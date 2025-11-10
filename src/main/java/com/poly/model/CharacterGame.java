// /EchoMMO/src/main/java/com/poly/model/CharacterGame.java
package com.poly.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "character_game")
public class CharacterGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Integer characterId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "level", columnDefinition = "INT DEFAULT 1")
    private Integer level = 1;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer experience = 0;

    @Column(columnDefinition = "INT DEFAULT 100")
    private Integer health = 100;

    @Column(name = "max_health", columnDefinition = "INT DEFAULT 100")
    private Integer maxHealth = 100;

    @Column(columnDefinition = "INT DEFAULT 10")
    private Integer atk = 10;

    @Column(columnDefinition = "INT DEFAULT 5")
    private Integer def = 5;

    @Column(columnDefinition = "INT DEFAULT 50")
    private Integer energy = 50;

    @Column(name = "max_energy", columnDefinition = "INT DEFAULT 50")
    private Integer maxEnergy = 50;

    @Column(name = "upgrade_points", columnDefinition = "INT DEFAULT 0")
    private Integer upgradePoints = 0;

    @Column(name = "created_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    private Set<CharacterGathering> gatheringLevels = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (level == null) level = 1;
        if (experience == null) experience = 0;
        if (maxHealth == null) maxHealth = 100;
        if (health == null) health = maxHealth;
        if (atk == null) atk = 10;
        if (def == null) def = 5;
        if (maxEnergy == null) maxEnergy = 50;
        if (energy == null) energy = maxEnergy;
        if (upgradePoints == null) upgradePoints = 0;
    }
     // Các phương thức getter/setter thủ công đã bị xóa
}