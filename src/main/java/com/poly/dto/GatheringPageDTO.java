package com.poly.dto;

import java.math.BigDecimal;

import com.poly.model.CharacterGame;
import com.poly.model.CharacterGathering;
import com.poly.model.Item;
import com.poly.model.Wallet;
import com.poly.service.CharacterService; // Import CharacterService for EXP calculation

import lombok.Data;

@Data
public class GatheringPageDTO {

    // Resource Info
    private String resourceType;
    private String resourceName;
    private String resourceIcon;
    private String requirementText; // e.g., "Yêu cầu cấp độ đào đá: 1"
    private Integer gatherLevel;     // Current gathering skill level
    private Double expPercent;       // Current EXP percentage for the skill
    private Long expToNextLevel;     // EXP needed for the next skill level
    private Integer requiredLevel;   // Required skill level to gather this resource
    private Integer energyCost;      // Energy cost per gather action

    // Player Info (from CharacterGame and Wallet)
    private BigDecimal playerGold;
    private Integer playerEnergy;
    private Integer playerMaxEnergy;
    private Integer playerHp;
    private Integer playerMaxHp;

    /**
     * Factory method to create DTO.
     * Requires CharacterService to be passed in.
     */
    public static GatheringPageDTO from(CharacterGathering cg, Item item, CharacterGame character, Wallet wallet, String icon, CharacterService characterService) {
        GatheringPageDTO dto = new GatheringPageDTO();

        // Calculate EXP needed for the gathering skill using CharacterService (assuming same formula for now)
        // You might want a separate EXP formula for gathering skills in GatheringService
        long expNeeded = characterService.calculateExpForNextLevel(cg.getLevel()); // TODO: Consider specific gathering EXP formula
        dto.setExpToNextLevel(expNeeded);
        dto.setExpPercent(Math.min(100.0, (cg.getExp() / (double) expNeeded) * 100.0));

        // Resource Info
        dto.setResourceType(cg.getResourceType());
        dto.setResourceName(item.getName());
        dto.setResourceIcon(icon);
        dto.setGatherLevel(cg.getLevel());
        dto.setRequiredLevel(1); // TODO: Make this dynamic based on Item or a separate config
        dto.setRequirementText("Yêu cầu cấp " + dto.getRequiredLevel() + " " + getSkillName(cg.getResourceType()));
        dto.setEnergyCost(5); // TODO: Make this dynamic?

        // Player Info from Character and Wallet
        dto.setPlayerGold(wallet.getBalance());
        dto.setPlayerEnergy(character.getEnergy());
        dto.setPlayerMaxEnergy(character.getMaxEnergy());
        dto.setPlayerHp(character.getHealth());
        dto.setPlayerMaxHp(character.getMaxHealth());

        return dto;
    }

    // Helper to get a user-friendly skill name
    private static String getSkillName(String resourceType) {
        return switch (resourceType) {
            case "stone" -> "Đào đá";
            case "wood" -> "Chặt gỗ";
            case "fish" -> "Câu cá";
            case "ore" -> "Khai khoáng";
            default -> "Thu thập";
        };
    }

    // *** FIX: Removed placeholder methods like setPlayerMaxHp, getRequiredLevel, etc. ***
    // Lombok's @Data annotation automatically generates these.
}