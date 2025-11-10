package com.poly.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poly.dto.EffectiveStatsDTO;
import com.poly.model.CharacterGame;
import com.poly.model.UserItem;
import com.poly.repository.CharacterGameRepository;
import com.poly.repository.UserItemRepository;

@Service
public class CharacterService {

    private static final Logger logger = LoggerFactory.getLogger(CharacterService.class);

    @Autowired private CharacterGameRepository characterRepo;
    @Autowired private UserItemRepository userItemRepo;

    // --- Level Up Configuration ---
    // EXP needed for level L = BASE * (RATE ^ (L-1))
    private static final double BASE_EXP_FOR_LEVEL = 100;
    private static final double EXP_GROWTH_RATE = 1.3;
    // Points gained per level
    private static final int POINTS_PER_LEVEL = 3;
    // Base stat increases per level
    private static final int HP_GAIN_PER_LEVEL = 10;
    private static final int ATK_GAIN_PER_LEVEL = 1;
    private static final int DEF_GAIN_PER_LEVEL = 1;
    // Stat increases per allocated point
    private static final int HP_GAIN_PER_POINT = 5;
    private static final int ATK_GAIN_PER_POINT = 1;
    private static final int DEF_GAIN_PER_POINT = 1;


    /**
     * Calculates the character's effective stats, including equipped items.
     */
    @Transactional(readOnly = true)
    public EffectiveStatsDTO getEffectiveStats(Integer characterId) {
        CharacterGame character = characterRepo.findById(characterId)
                .orElseThrow(() -> new RuntimeException("Character not found with ID: " + characterId));

        // Get currently equipped items for the character's user
        List<UserItem> equippedItems = userItemRepo.findByUser_UserIdAndIsEquippedTrue(character.getUser().getUserId());

        // Start with base stats
        int totalAtk = character.getAtk() != null ? character.getAtk() : 0;
        int totalDef = character.getDef() != null ? character.getDef() : 0;
        int totalMaxHp = character.getMaxHealth() != null ? character.getMaxHealth() : 0;

        // Add stats from equipped items
        for (UserItem ui : equippedItems) {
            if (ui.getItem() != null) {
                totalAtk += ui.getItem().getGameAtk() != null ? ui.getItem().getGameAtk() : 0;
                totalDef += ui.getItem().getGameDef() != null ? ui.getItem().getGameDef() : 0;
                // Add other stats like HP from items if applicable
                // totalMaxHp += ui.getItem().getGameHpBonus() != null ? ui.getItem().getGameHpBonus() : 0;
            }
        }
        logger.debug("Calculated effective stats for character {}: ATK={}, DEF={}, MaxHP={}", characterId, totalAtk, totalDef, totalMaxHp);
        return new EffectiveStatsDTO(totalAtk, totalDef, totalMaxHp);
    }

    /**
     * Checks if the character has enough EXP to level up and processes the level up(s).
     * Increases base stats, grants upgrade points, and refills HP.
     * @return true if the character leveled up, false otherwise.
     */
    @Transactional
    public boolean checkAndProcessLevelUp(Integer characterId) {
        CharacterGame character = characterRepo.findById(characterId).orElseThrow(() -> new RuntimeException("Character not found"));
        boolean leveledUp = false;

        long expNeeded = calculateExpForNextLevel(character.getLevel());

        // Loop in case of multiple level ups from a large EXP gain
        while (character.getExperience() >= expNeeded) {
            character.setExperience(character.getExperience() - (int)expNeeded); // Deduct EXP for current level
            character.setLevel(character.getLevel() + 1);

            // Increase base stats
            character.setMaxHealth(character.getMaxHealth() + HP_GAIN_PER_LEVEL);
            character.setAtk(character.getAtk() + ATK_GAIN_PER_LEVEL);
            character.setDef(character.getDef() + DEF_GAIN_PER_LEVEL);

            // Refill health and energy on level up
            character.setHealth(character.getMaxHealth());
            character.setEnergy(character.getMaxEnergy());

            // Grant upgrade points
            character.setUpgradePoints(character.getUpgradePoints() + POINTS_PER_LEVEL);

            leveledUp = true;
            logger.info("Character {} leveled up to {}!", character.getCharacterId(), character.getLevel());

            // Calculate EXP needed for the *new* next level
            expNeeded = calculateExpForNextLevel(character.getLevel());
        }

        if (leveledUp) {
            characterRepo.save(character); // Save the updated character state
        }
        return leveledUp;
    }

    /**
     * Calculates the total EXP required to reach the next level from the start of the current level.
     */
    public long calculateExpForNextLevel(int currentLevel) {
        if (currentLevel < 1) return (long) BASE_EXP_FOR_LEVEL; // Should not happen, but safeguard
        // Formula: base * (rate ^ (level - 1))
        return (long) Math.floor(BASE_EXP_FOR_LEVEL * Math.pow(EXP_GROWTH_RATE, currentLevel - 1));
    }

    /**
     * Allocates one upgrade point to the specified stat type.
     * Throws RuntimeException if not enough points or invalid stat type.
     */
    @Transactional
    public CharacterGame allocateStatPoint(Integer characterId, String statType) {
        CharacterGame character = characterRepo.findById(characterId)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        if (character.getUpgradePoints() == null || character.getUpgradePoints() <= 0) {
            throw new RuntimeException("Không đủ điểm nâng cấp!");
        }

        boolean statUpdated = false;
        switch (statType.toLowerCase()) {
            case "maxhp":
                character.setMaxHealth(character.getMaxHealth() + HP_GAIN_PER_POINT);
                // Optionally, increase current HP proportionally or fully heal
                character.setHealth(character.getHealth() + HP_GAIN_PER_POINT); // Simple increase, capped below
                 if(character.getHealth() > character.getMaxHealth()) character.setHealth(character.getMaxHealth());
                statUpdated = true;
                break;
            case "atk":
                character.setAtk(character.getAtk() + ATK_GAIN_PER_POINT);
                statUpdated = true;
                break;
            case "def":
                character.setDef(character.getDef() + DEF_GAIN_PER_POINT);
                statUpdated = true;
                break;
            default:
                logger.warn("Invalid stat type allocation attempt: {}", statType);
                throw new RuntimeException("Loại chỉ số không hợp lệ: " + statType);
        }

        if(statUpdated){
            character.setUpgradePoints(character.getUpgradePoints() - 1);
            logger.info("Allocated point to {} for character {}. Points remaining: {}", statType, characterId, character.getUpgradePoints());
            return characterRepo.save(character);
        } else {
             // Should not happen if switch case covers all valid types
             throw new RuntimeException("Không thể cập nhật chỉ số.");
        }

    }
}