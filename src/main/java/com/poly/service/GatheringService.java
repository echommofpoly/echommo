package com.poly.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
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
import com.poly.dto.GatheringPageDTO;
import com.poly.dto.GatheringResponseDTO;
import com.poly.model.CharacterGame;
import com.poly.model.CharacterGathering;
import com.poly.model.Item;
import com.poly.model.User;
import com.poly.model.UserItem;
import com.poly.model.Wallet;
import com.poly.repository.CharacterGameRepository;
import com.poly.repository.CharacterGatheringRepository;
import com.poly.repository.ItemRepository;
import com.poly.repository.UserItemRepository;
import com.poly.repository.UserRepository;
import com.poly.repository.WalletRepository;

@Service
public class GatheringService {

    private static final Logger logger = LoggerFactory.getLogger(GatheringService.class);
    private final Random random = new Random();

    private static final int BASE_GATHERING_ENERGY_COST = 5;
    private static final int BASE_GATHERING_EXP_GAIN = 10;
    // Constants for gathering level EXP calculation
    private static final double BASE_GATHERING_EXP_FOR_LEVEL = 50.0;
    private static final double GATHERING_EXP_GROWTH_RATE = 1.2;

    private static final Map<String, String> RESOURCE_TO_ITEM_NAME = Map.of(
            "stone", "Đá thô",
            "wood", "Gỗ mục",
            "fish", "Cá nhỏ",
            "ore", "Quặng đồng"
    );

    private static final Map<String, String> RESOURCE_TO_ICON = Map.of(
            "stone", "🪨",
            "wood", "🌲",
            "fish", "🐟",
            "ore", "⛏️"
    );

    @Autowired private UserRepository userRepository;
    @Autowired private CharacterGameRepository characterGameRepo;
    @Autowired private CharacterGatheringRepository gatheringRepo;
    @Autowired private WalletRepository walletRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserItemRepository userItemRepo; // Correct declaration
    @Autowired private CharacterService characterService; // Keep for potential reuse or consistency

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

    @Transactional(readOnly = true)
    public GatheringPageDTO getGatheringPageData(String resourceType) {
        User user = getCurrentUser();
        CharacterGame character = getCharacter(user.getUserId());
        Wallet wallet = walletRepo.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        CharacterGathering cg = gatheringRepo.findByCharacter_CharacterIdAndResourceType(
                character.getCharacterId(), resourceType)
                .orElseThrow(() -> new RuntimeException("Kỹ năng thu thập '" + resourceType + "' không tồn tại cho nhân vật."));

        String itemName = RESOURCE_TO_ITEM_NAME.getOrDefault(resourceType, "Unknown Resource Item");
        Item item = itemRepo.findByNameIgnoreCase(itemName)
                .orElseThrow(() -> new RuntimeException("Vật phẩm '" + itemName + "' tương ứng với tài nguyên '" + resourceType + "' không tồn tại trong bảng Item."));

        String icon = RESOURCE_TO_ICON.getOrDefault(resourceType, "❓");

        logger.debug("Loading gathering page for user {}, resource {}", user.getUsername(), resourceType);
        // Pass CharacterService if GatheringPageDTO needs it (it does currently)
        return GatheringPageDTO.from(cg, item, character, wallet, icon, characterService);
    }

    @Transactional
    public ApiResponse<GatheringResponseDTO> doGather(String resourceType) {
        User user = getCurrentUser();
        CharacterGame character = getCharacter(user.getUserId());
        Wallet wallet = walletRepo.findByUser_UserId(user.getUserId()).orElseThrow();
        CharacterGathering cg = gatheringRepo.findByCharacter_CharacterIdAndResourceType(
                character.getCharacterId(), resourceType)
                .orElseThrow(() -> new RuntimeException("Kỹ năng thu thập không tồn tại: " + resourceType));

        int energyCost = BASE_GATHERING_ENERGY_COST; // Can be adjusted based on level/tool later
        if (character.getEnergy() < energyCost) {
            logger.warn("User {} attempted to gather {} with insufficient energy ({} < {})",
                    user.getUsername(), resourceType, character.getEnergy(), energyCost);
            return ApiResponse.error("Không đủ năng lượng!");
        }
        character.setEnergy(character.getEnergy() - energyCost);

        String itemName = RESOURCE_TO_ITEM_NAME.get(resourceType);
        Item item = itemRepo.findByNameIgnoreCase(itemName)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemName));

        // Calculate amount based on skill level (example: 1 + random up to level/2 + 1)
        int amount = 1 + random.nextInt(Math.max(1, cg.getLevel() / 2 + 1));
        addItemToInventory(user, item, amount);

        // Calculate EXP gain (example: base * level)
        int expGained = BASE_GATHERING_EXP_GAIN * cg.getLevel();

        cg.setExp(cg.getExp() + expGained);

        // Check for level up
        boolean levelUp = checkGatheringLevelUp(cg);

        // Save changes
        characterGameRepo.save(character);
        gatheringRepo.save(cg);

        // Prepare response
        GatheringResponseDTO response = new GatheringResponseDTO();
        response.setMessage(String.format("Bạn nhận được +%d %s, +%d EXP!", amount, itemName, expGained));
        response.setLevel(cg.getLevel());
        response.setExp(cg.getExp()); // Current EXP after gain
        response.setLevelUp(levelUp);
        response.setPlayerEnergy(character.getEnergy());
        response.setPlayerGold(wallet.getBalance());

        // Calculate EXP needed for the *next* level
        long expNeeded = calculateExpForGatheringNextLevel(cg.getLevel());
        response.setExpToNextLevel(expNeeded);

        // Calculate current EXP percentage towards next level
        double expPercent = (expNeeded > 0) ? Math.min(100.0, (cg.getExp() / (double) expNeeded) * 100.0) : 0.0;
        response.setExpPercent(expPercent);

        if (levelUp) {
            response.setMessage(response.getMessage() +
                    String.format(" Kỹ năng %s đã lên cấp %d!", getSkillName(resourceType), cg.getLevel()));
        }

        logger.info("User {} gathered {}: +{} {}, +{} EXP. Level up: {}",
                user.getUsername(), resourceType, amount, itemName, expGained, levelUp);

        return ApiResponse.success(response.getMessage(), response);
    }

    // *** FIX: Corrected variable name in this method ***
    private void addItemToInventory(User user, Item item, int quantity) {
        if (quantity <= 0) return;

        // Use 'userItemRepo' here (Line 169 in error)
        Optional<UserItem> existingOpt = userItemRepo.findByUser_UserIdAndItem_ItemId(
                user.getUserId(), item.getItemId());

        if (existingOpt.isPresent()) {
            UserItem ui = existingOpt.get();
            ui.setQuantity(ui.getQuantity() + quantity);
            // Use 'userItemRepo' here (Line 175 in error)
            userItemRepo.save(ui);
            logger.debug("Added {} quantity to existing UserItem {} for user {}",
                    quantity, item.getName(), user.getUsername());
        } else {
            UserItem ui = new UserItem();
            ui.setUser(user);
            ui.setItem(item);
            ui.setQuantity(quantity);
            // Use 'userItemRepo' here (Line 183 in error)
            userItemRepo.save(ui);
            logger.debug("Created new UserItem {} (x{}) for user {}",
                    item.getName(), quantity, user.getUsername());
        }
    }

    private boolean checkGatheringLevelUp(CharacterGathering cg) {
        boolean leveledUp = false;
        long expNeeded = calculateExpForGatheringNextLevel(cg.getLevel());
        int currentExp = cg.getExp(); // Get current Integer EXP

        // Loop in case of multiple level ups
        while (currentExp >= expNeeded) {
            currentExp -= (int)expNeeded; // Deduct EXP for current level
            cg.setLevel(cg.getLevel() + 1);
            leveledUp = true;
            logger.info("Gathering skill {} leveled up to {} for character {}",
                    cg.getResourceType(), cg.getLevel(), cg.getCharacter().getCharacterId());

            // Calculate EXP needed for the *new* next level
            expNeeded = calculateExpForGatheringNextLevel(cg.getLevel());
        }

        cg.setExp(currentExp); // Set the remaining EXP
        return leveledUp;
    }

    public long calculateExpForGatheringNextLevel(int currentLevel) {
        if (currentLevel < 1) {
            return (long) BASE_GATHERING_EXP_FOR_LEVEL;
        }
        // Formula: base * (rate ^ (level - 1))
        double expDouble = BASE_GATHERING_EXP_FOR_LEVEL * Math.pow(GATHERING_EXP_GROWTH_RATE, currentLevel - 1);
        return (long) Math.floor(expDouble); // Return the total EXP needed for the next level threshold
    }

    private String getSkillName(String resourceType) {
        return switch (resourceType) {
            case "stone" -> "Đào đá";
            case "wood" -> "Chặt gỗ";
            case "fish" -> "Câu cá";
            case "ore" -> "Khai khoáng";
            default -> "Thu thập";
        };
    }
}