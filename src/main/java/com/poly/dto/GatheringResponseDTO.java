package com.poly.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class GatheringResponseDTO {
    private String message;     // Feedback message (e.g., "+2 Stone, +20 EXP!")
    private Integer level;      // Updated skill level
    private Integer exp;        // Updated skill EXP
    private Double expPercent;  // Updated skill EXP percentage
    private Long expToNextLevel; // Updated EXP needed for next level
    private boolean levelUp;    // Flag indicating if the skill leveled up

    // Updated Player Stats
    private Integer playerEnergy;
    private BigDecimal playerGold; // Gold doesn't usually change here, but good to return consistent state
}