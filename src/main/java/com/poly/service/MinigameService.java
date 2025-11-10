package com.poly.service;

import java.math.BigDecimal;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poly.dto.ApiResponse;
import com.poly.dto.MinigameAdventureResponse;
import com.poly.dto.MinigameDataDTO;
import com.poly.model.CharacterGame;
import com.poly.model.Enemy;
import com.poly.model.User;
import com.poly.model.Wallet;
import com.poly.repository.CharacterGameRepository;
import com.poly.repository.EnemyRepository;
import com.poly.repository.UserRepository;
import com.poly.repository.WalletRepository;

@Service
public class MinigameService {

    private static final Logger logger = LoggerFactory.getLogger(MinigameService.class);
    private final Random random = new Random();

    // --- Configuration ---
    private static final int ADVENTURE_ENERGY_COST = 10;
    private static final int REST_ENERGY_GAIN = 20;
    private static final int REST_HP_GAIN = 10;
    // Encounter Probabilities
    private static final double ENEMY_CHANCE = 0.20; // 20%
    private static final double RESOURCE_CHANCE = 0.40; // 40% (Total: 60%)
    private static final double GOLD_CHANCE = 0.25; // 25% (Total: 85%)
    // Remaining 15% is EXP_CHANCE


    @Autowired private UserRepository userRepository;
    @Autowired private CharacterGameRepository characterGameRepo;
    @Autowired private WalletRepository walletRepo;
    @Autowired private EnemyRepository enemyRepo; // For enemy encounters
    @Autowired private CharacterService characterService; // For level up checks

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private CharacterGame getCharacter(Integer userId) {
        return characterGameRepo.findFirstByUserIdOrderByCharacterIdAsc(userId)
                .orElseThrow(() -> new RuntimeException("Character not found for user ID: " + userId));
    }

    /**
     * Handles the "Adventure" action. Deducts energy, determines encounter,
     * updates state (if gold/exp found), and returns encounter details.
     */
    @Transactional
    public ApiResponse<MinigameAdventureResponse> doAdventure() {
        User user = getCurrentUser();
        CharacterGame character = getCharacter(user.getUserId());
        Wallet wallet = walletRepo.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // 1. Check Energy
        if (character.getEnergy() < ADVENTURE_ENERGY_COST) {
            logger.warn("User {} tried to adventure with insufficient energy.", user.getUsername());
            return ApiResponse.error("Không đủ năng lượng! Hãy nghỉ ngơi.");
        }
        character.setEnergy(character.getEnergy() - ADVENTURE_ENERGY_COST);
        logger.debug("User {} starts adventure, energy reduced to {}", user.getUsername(), character.getEnergy());

        double encounterRoll = random.nextDouble();
        MinigameAdventureResponse response = new MinigameAdventureResponse();
        boolean stateChanged = true; // Assume state changed initially (energy deduction)

        // 2. Determine Encounter Type
        if (encounterRoll < ENEMY_CHANCE) { // Enemy Encounter
            Enemy enemy = enemyRepo.findRandomEnemy()
                    .orElseThrow(() -> new RuntimeException("Không có quái vật nào trong CSDL!"));
            response.setMessage("Bạn gặp " + enemy.getName() + "!");
            response.setEncounterIcon(enemy.getEmojiIcon());
            response.setEncounterRedirect("/battle/" + enemy.getEnemyId()); // Redirect to battle page
            logger.info("User {} encountered enemy {}", user.getUsername(), enemy.getName());
            // State saved later before returning DTO

        } else if (encounterRoll < ENEMY_CHANCE + RESOURCE_CHANCE) { // Resource Encounter
            String[] resources = {"stone", "wood", "fish", "ore"}; // Available resources
            String[] icons = {"🪨", "🌲", "🐟", "⛏️"};
            int index = random.nextInt(resources.length);
            String foundResource = resources[index];

            response.setMessage("Bạn tìm thấy khu vực có " + getResourceName(foundResource) + "!");
            response.setEncounterIcon(icons[index]);
            response.setEncounterRedirect("/gathering/" + foundResource); // Redirect to gathering page
            logger.info("User {} found resource node: {}", user.getUsername(), foundResource);
            // State saved later before returning DTO

        } else if (encounterRoll < ENEMY_CHANCE + RESOURCE_CHANCE + GOLD_CHANCE) { // Gold Found
            int goldGained = random.nextInt(11) + 10; // 10-20 gold
            wallet.setBalance(wallet.getBalance().add(new BigDecimal(goldGained)));
            walletRepo.save(wallet); // Save wallet changes immediately
            response.setMessage("Bạn nhặt được " + goldGained + " vàng!");
            response.setEncounterIcon("🪙");
            logger.info("User {} found {} gold", user.getUsername(), goldGained);

        } else { // EXP Found
            int expGained = random.nextInt(21) + 15; // 15-35 EXP
            character.setExperience(character.getExperience() + expGained);
            response.setMessage("Bạn nhận được " + expGained + " EXP!");
            response.setEncounterIcon("✨");
            logger.info("User {} gained {} EXP", user.getUsername(), expGained);

            // Check for Level Up after gaining EXP
            boolean leveledUp = characterService.checkAndProcessLevelUp(character.getCharacterId());
            if (leveledUp) {
                // Fetch updated level to include in message
                CharacterGame updatedCharacter = characterGameRepo.findById(character.getCharacterId()).orElse(character);
                 response.setMessage(response.getMessage() + String.format(" Chúc mừng lên cấp %d!", updatedCharacter.getLevel()));
                 // Character state saved within checkAndProcessLevelUp
                 stateChanged = false; // Prevent double saving if level up occurred
            }
        }

        // 3. Save character state if it changed and wasn't saved by level up
        if (stateChanged) {
             characterGameRepo.save(character);
        }

        // 4. Create and return response DTO
        long expToNext = characterService.calculateExpForNextLevel(character.getLevel());
        response.setUpdatedStats(MinigameDataDTO.from(character, wallet.getBalance(), expToNext));
        return ApiResponse.success(response.getMessage(), response);
    }

    /**
     * Handles the "Rest" action. Recovers HP and Energy.
     */
    @Transactional
    public ApiResponse<MinigameDataDTO> doRest() {
        User user = getCurrentUser();
        CharacterGame character = getCharacter(user.getUserId());
        Wallet wallet = walletRepo.findByUser_UserId(user.getUserId()).orElseThrow();

        boolean alreadyFull = character.getEnergy() >= character.getMaxEnergy() &&
                              character.getHealth() >= character.getMaxHealth();

        if (alreadyFull) {
            logger.debug("User {} attempted to rest while already full.", user.getUsername());
             long expToNext = characterService.calculateExpForNextLevel(character.getLevel());
            return ApiResponse.success("Năng lượng và Máu đã đầy!", MinigameDataDTO.from(character, wallet.getBalance(), expToNext)); // Return success but indicate no change
        }

        int energyGained = Math.min(REST_ENERGY_GAIN, character.getMaxEnergy() - character.getEnergy());
        int hpGained = Math.min(REST_HP_GAIN, character.getMaxHealth() - character.getHealth());

        character.setEnergy(character.getEnergy() + energyGained);
        character.setHealth(character.getHealth() + hpGained);

        characterGameRepo.save(character);
        logger.info("User {} rested. Gained {} energy, {} HP.", user.getUsername(), energyGained, hpGained);

         long expToNext = characterService.calculateExpForNextLevel(character.getLevel());
        return ApiResponse.success(String.format("Nghỉ ngơi... +%d Energy, +%d HP", energyGained, hpGained),
                                MinigameDataDTO.from(character, wallet.getBalance(), expToNext));
    }

    // Helper to get resource name for messages
    private String getResourceName(String resourceType) {
        return switch (resourceType) {
            case "stone" -> "Đá";
            case "wood" -> "Gỗ";
            case "fish" -> "Cá";
            case "ore" -> "Quặng";
            default -> "Tài nguyên";
        };
    }
}