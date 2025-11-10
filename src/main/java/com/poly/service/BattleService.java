package com.poly.service;

import java.math.BigDecimal;
import java.util.List;
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
import com.poly.dto.BattleActionResponseDTO;
import com.poly.dto.BattleStateDTO;
import com.poly.dto.EffectiveStatsDTO;
import com.poly.model.CharacterGame;
import com.poly.model.Enemy;
import com.poly.model.Item; // Ensure Item is imported
import com.poly.model.User;
import com.poly.model.UserItem;
import com.poly.model.Wallet;
import com.poly.repository.CharacterGameRepository;
import com.poly.repository.EnemyRepository;
import com.poly.repository.UserItemRepository;
import com.poly.repository.UserRepository;
import com.poly.repository.WalletRepository;

@Service
public class BattleService {

    private static final Logger logger = LoggerFactory.getLogger(BattleService.class);
    private final Random random = new Random();

    @Autowired private UserRepository userRepository;
    @Autowired private CharacterGameRepository characterGameRepo;
    @Autowired private EnemyRepository enemyRepo;
    @Autowired private WalletRepository walletRepo;
    @Autowired private UserItemRepository userItemRepo;
    @Autowired private CharacterService characterService; // For stats and level up

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private CharacterGame getCharacter(Integer userId) {
        // Assuming one character per user for now
        return characterGameRepo.findFirstByUserIdOrderByCharacterIdAsc(userId)
                .orElseThrow(() -> new RuntimeException("Character not found for user ID: " + userId));
    }

    /**
     * Gets the initial state for the battle page.
     */
    @Transactional(readOnly = true)
    public BattleStateDTO getBattleState(Integer enemyId) {
        User user = getCurrentUser();
        CharacterGame character = getCharacter(user.getUserId());
        Enemy enemy = enemyRepo.findById(enemyId)
                .orElseThrow(() -> new RuntimeException("Enemy not found with ID: " + enemyId));

        // Get effective stats including equipment
        EffectiveStatsDTO effectiveStats = characterService.getEffectiveStats(character.getCharacterId());

        // Get available potions
        List<UserItem> potions = userItemRepo.findByUser_UserIdAndItem_ItemCategoryAndQuantityGreaterThan(user.getUserId(), "potion", 0);

        logger.info("Starting battle for user {} against enemy {}", user.getUsername(), enemy.getName());
        return BattleStateDTO.from(character, enemy, effectiveStats, potions);
    }

    /**
     * Handles the Player's Attack action.
     */
    @Transactional
    public ApiResponse<BattleActionResponseDTO> doAttack(Integer enemyId, Integer currentEnemyHp) {
        User user = getCurrentUser();
        CharacterGame character = getCharacter(user.getUserId());
        Enemy enemy = enemyRepo.findById(enemyId).orElseThrow(() -> new RuntimeException("Enemy not found"));
        Wallet wallet = walletRepo.findByUser_UserId(user.getUserId()).orElseThrow(() -> new RuntimeException("Wallet not found"));

        BattleActionResponseDTO response = new BattleActionResponseDTO();

        // 1. Get effective player stats
        EffectiveStatsDTO playerStats = characterService.getEffectiveStats(character.getCharacterId());

        // 2. Player attacks enemy
        int playerDmg = Math.max(1, playerStats.getAtk() - enemy.getDef());
        currentEnemyHp = Math.max(0, currentEnemyHp - playerDmg);
        response.addLog(String.format("⚔️ Bạn tấn công %s, gây %d sát thương!", enemy.getName(), playerDmg));
        response.setEnemyHp(currentEnemyHp);

        // 3. Check if enemy is defeated
        if (currentEnemyHp <= 0) {
            // *** FIX: Pass the correct arguments to handleVictory ***
            return handleVictory(character, enemy, wallet, response); // Victory handled, returns response
        }

        // 4. Enemy attacks player
        // Consider adding randomness or special attacks later
        int enemyDmg = Math.max(1, enemy.getAtk() - playerStats.getDef());
        character.setHealth(Math.max(0, character.getHealth() - enemyDmg));
        response.addLog(String.format("👹 %s tấn công, bạn nhận %d sát thương!", enemy.getName(), enemyDmg));
        response.setPlayerHp(character.getHealth());

        // 5. Check if player is defeated
        if (character.getHealth() <= 0) {
            response.addLog("💀 Bạn đã bị đánh bại!");
            response.setBattleStatus(2); // Status: Lost
            // Optional: Handle defeat consequences (e.g., respawn with 1 HP, lose gold/exp)
            character.setHealth(1); // Respawn with 1 HP
            // characterGameRepo.save(character); // Save the 1 HP state (done below)
        }

        // Save character state (HP change) if battle is ongoing or player lost but respawned
        if (response.getBattleStatus() == 0 || response.getBattleStatus() == 2) {
             characterGameRepo.save(character);
        }

        logger.debug("Attack turn completed. Player HP: {}, Enemy HP: {}", character.getHealth(), currentEnemyHp);
        return ApiResponse.success("Lượt đánh thành công", response);
    }

    /**
     * Handles the Player's Use Item (Potion) action.
     */
    @Transactional
    public ApiResponse<BattleActionResponseDTO> doUseItem(Integer userItemId, Integer enemyId, Integer currentEnemyHp) {
        User user = getCurrentUser();
        CharacterGame character = getCharacter(user.getUserId());
        Enemy enemy = enemyRepo.findById(enemyId).orElseThrow(() -> new RuntimeException("Enemy not found"));
        UserItem userItem = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại trong kho đồ!"));

        // Validation
        if (!userItem.getUser().getUserId().equals(user.getUserId())) {
            return ApiResponse.error("Không có quyền sử dụng vật phẩm này!");
        }

        // *** FIX: Cast userItem.getItem() to Item BEFORE using it ***
        Item item = (Item) userItem.getItem();
        if (item == null || !"potion".equalsIgnoreCase(item.getItemCategory())) {
            return ApiResponse.error("Vật phẩm này không phải là Potion!");
        }
        if (userItem.getQuantity() <= 0) {
            // *** FIX: Use 'item.getName()' instead of logger ***
            return ApiResponse.error("Đã hết " + item.getName() + "!");
        }
        if (character.getHealth() >= character.getMaxHealth()) {
            return ApiResponse.error("Máu đã đầy!");
        }

        BattleActionResponseDTO response = new BattleActionResponseDTO();

        // 1. Apply Potion effect
        // *** FIX: Use the 'item' variable which is already casted ***
        int healAmount = item.getGameHeal() != null ? item.getGameHeal() : 0;
        int actualHeal = Math.min(healAmount, character.getMaxHealth() - character.getHealth());
        character.setHealth(character.getHealth() + actualHeal);
        // *** FIX: Use the 'item' variable ***
        response.addLog(String.format("💊 Bạn dùng %s, hồi %d HP!", item.getName(), actualHeal));

        // 2. Consume Potion
        userItem.setQuantity(userItem.getQuantity() - 1);
        response.setUserItemId(userItemId);
        response.setUpdatedPotionQty(userItem.getQuantity());
        if (userItem.getQuantity() <= 0) {
            userItemRepo.delete(userItem);
            // *** FIX: Use the 'item' variable ***
            logger.info("Deleted empty potion stack: {}", item.getName());
        } else {
            userItemRepo.save(userItem);
        }

        // 3. Enemy attacks player (using item takes a turn)
        EffectiveStatsDTO playerStats = characterService.getEffectiveStats(character.getCharacterId());
        int enemyDmg = Math.max(1, enemy.getAtk() - playerStats.getDef());
        character.setHealth(Math.max(0, character.getHealth() - enemyDmg));
        response.addLog(String.format("👹 %s tấn công khi bạn đang dùng item, nhận %d sát thương!", enemy.getName(), enemyDmg));

        // 4. Update response HP and check defeat
        response.setPlayerHp(character.getHealth());
        response.setEnemyHp(currentEnemyHp); // Enemy HP doesn't change on player's item use
        if (character.getHealth() <= 0) {
            response.addLog("💀 Bạn đã bị đánh bại ngay sau khi dùng Potion!");
            response.setBattleStatus(2); // Lost
            character.setHealth(1); // Respawn
        }

        // Save character state
        characterGameRepo.save(character);
        logger.debug("Use Item turn completed. Player HP: {}, Enemy HP: {}", character.getHealth(), currentEnemyHp);
        return ApiResponse.success("Dùng vật phẩm thành công", response);
    }

    /**
     * Handles logic when the player wins the battle.
     */
    @Transactional // This method modifies state, needs transaction
    private ApiResponse<BattleActionResponseDTO> handleVictory(CharacterGame character, Enemy enemy, Wallet wallet, BattleActionResponseDTO response) {
        response.addLog("🎉 " + enemy.getName() + " đã bị đánh bại!");
        response.setBattleStatus(1); // Status: Won

        // Calculate rewards
        int expGained = 0;
        if (enemy.getRewardExpMin() != null && enemy.getRewardExpMax() != null && enemy.getRewardExpMax() >= enemy.getRewardExpMin()) {
             expGained = random.nextInt(enemy.getRewardExpMax() - enemy.getRewardExpMin() + 1) + enemy.getRewardExpMin();
        }

        int goldGained = 0;
        if(enemy.getRewardGoldMin() != null && enemy.getRewardGoldMax() != null && enemy.getRewardGoldMax() >= enemy.getRewardGoldMin()){
             goldGained = random.nextInt(enemy.getRewardGoldMax() - enemy.getRewardGoldMin() + 1) + enemy.getRewardGoldMin();
        }


        // Apply rewards
        character.setExperience(character.getExperience() + expGained);
        wallet.setBalance(wallet.getBalance().add(new BigDecimal(goldGained)));

        // Check for character level up
        boolean leveledUp = characterService.checkAndProcessLevelUp(character.getCharacterId());
        // Note: checkAndProcessLevelUp saves the character if leveled up

        response.setRewardExp(expGained);
        response.setRewardGold(new BigDecimal(goldGained));
        response.addLog(String.format("Bạn nhận được %d EXP và %d Vàng!", expGained, goldGained));

        if (leveledUp) {
            // Fetch the updated character level after level up
            // Re-fetch character to get updated stats if CharacterService doesn't return it
             CharacterGame updatedCharacter = characterGameRepo.findById(character.getCharacterId()).orElse(character);
             response.addLog(String.format("✨ CHÚC MỪNG BẠN ĐÃ LÊN CẤP %d! Bạn nhận được điểm nâng cấp.", updatedCharacter.getLevel()));
             // Character state already saved by CharacterService
        } else {
             // If no level up, save the character with added experience
             characterGameRepo.save(character);
        }

        // Save wallet changes
        walletRepo.save(wallet);

        logger.info("User {} defeated enemy {} and gained {} EXP, {} Gold. Leveled up: {}",
                 character.getUser().getUsername(), enemy.getName(), expGained, goldGained, leveledUp);
        // *** FIX: Return ApiResponse.success with the updated response DTO ***
        return ApiResponse.success("Chiến thắng!", response);
     }
}