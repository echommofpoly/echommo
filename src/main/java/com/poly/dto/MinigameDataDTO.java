package com.poly.dto;

import java.math.BigDecimal;

import com.poly.model.CharacterGame;

import lombok.Data;

@Data
public class MinigameDataDTO {
    // Character Stats
    private Integer health;
    private Integer maxHealth;
    private Integer energy;
    private Integer maxEnergy;
    private Integer level;
    private Integer atk; // Base ATK
    private Integer def; // Base DEF
    private Integer experience;
    private Long expToNextLevel; // Added for display

    // Wallet Balance
    private BigDecimal gold;

    /**
     * Converts from CharacterGame entity and Wallet balance to DTO.
     * Requires the calculated EXP needed for the next level.
     */
    public static MinigameDataDTO from(CharacterGame c, BigDecimal gold, long expToNext) {
        MinigameDataDTO dto = new MinigameDataDTO();
        if (c != null) {
            dto.setHealth(c.getHealth());
            dto.setMaxHealth(c.getMaxHealth());
            dto.setEnergy(c.getEnergy());
            dto.setMaxEnergy(c.getMaxEnergy());
            dto.setLevel(c.getLevel());
            dto.setAtk(c.getAtk());
            dto.setDef(c.getDef());
            dto.setExperience(c.getExperience());
            dto.setExpToNextLevel(expToNext); // Set calculated value
        } else {
            // Default values if character is null (e.g., during registration display)
            dto.setHealth(100); dto.setMaxHealth(100);
            dto.setEnergy(50); dto.setMaxEnergy(50);
            dto.setLevel(1); dto.setAtk(10); dto.setDef(5);
            dto.setExperience(0); dto.setExpToNextLevel(expToNext); // Exp for level 1
        }
        dto.setGold(gold != null ? gold : BigDecimal.ZERO);
        return dto;
    }

    // *** FIX: Removed placeholder methods like setGold, setExpToNextLevel, etc. ***
    // Lombok's @Data annotation automatically generates these.
}