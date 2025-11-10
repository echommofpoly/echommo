package com.poly.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.poly.dto.ApiResponse;
import com.poly.model.CharacterGame;
import com.poly.model.Item; // Ensure Item is imported
import com.poly.model.User;
import com.poly.model.UserItem;
import com.poly.repository.UserItemRepository;
import com.poly.repository.UserRepository;
import com.poly.service.CharacterService; // Inject CharacterService

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired private UserItemRepository userItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CharacterService characterService; // Inject CharacterService

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @GetMapping
    public String showInventory(Model model) {
        User user = getCurrentUser();
        List<UserItem> userItems = userItemRepository.findByUser_UserIdAndQuantityGreaterThan(user.getUserId(), 0);

        List<Map<String, Object>> items = userItems.stream()
                .filter(ui -> ui.getItem() != null)
                .map(ui -> {
                    Map<String, Object> map = new HashMap<>();
                    Item item = (Item) ui.getItem(); // Cast to Item
                    map.put("userItemId", ui.getUserItemId());
                    map.put("itemId", item.getItemId());
                    map.put("name", item.getName());
                    map.put("description", item.getDescription());
                    map.put("rarity", item.getRarity());
                    map.put("category", item.getItemCategory());
                    map.put("quantity", ui.getQuantity());
                    map.put("basePrice", item.getBasePrice());
                    map.put("gameAtk", item.getGameAtk());
                    map.put("gameDef", item.getGameDef());
                    map.put("gameHeal", item.getGameHeal());
                    map.put("gameSlot", item.getGameSlot());
                    map.put("imageUrl", item.getImageUrl());
                    map.put("isEquipped", ui.getIsEquipped());
                    return map;
                }).collect(Collectors.toList());

        model.addAttribute("items", items);
        model.addAttribute("user", user); // Pass user for wallet balance display

        return "inventory/index";
    }

    @PostMapping("/equip/{userItemId}")
    @ResponseBody
    @Transactional // Ensure atomicity
    public ApiResponse<Void> equipItem(@PathVariable Integer userItemId) {
        try {
            User user = getCurrentUser();
            UserItem itemToEquip = userItemRepository.findById(userItemId)
                    .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại trong kho đồ!"));

            // Authorization check
            if (!itemToEquip.getUser().getUserId().equals(user.getUserId())) {
                return ApiResponse.error("Không có quyền trang bị vật phẩm này!");
            }

            Item itemData = (Item) itemToEquip.getItem(); // Cast to Item
            if (itemData == null || itemData.getGameSlot() == null || itemData.getGameSlot().isEmpty()) {
                return ApiResponse.error("Vật phẩm này không thể trang bị!");
            }
             if (Boolean.TRUE.equals(itemToEquip.getIsEquipped())) {
                 return ApiResponse.error("Vật phẩm đã được trang bị!");
             }


            String slot = itemData.getGameSlot();

            // Unequip existing item in the same slot
            Optional<UserItem> currentlyEquippedOpt = userItemRepository.findByUser_UserIdAndItem_GameSlotAndIsEquippedTrue(user.getUserId(), slot);
            if(currentlyEquippedOpt.isPresent()){
                UserItem currentlyEquipped = currentlyEquippedOpt.get();
                if(!currentlyEquipped.getUserItemId().equals(userItemId)){ // Make sure we don't unequip the item we are trying to equip if it somehow gets here
                     currentlyEquipped.setIsEquipped(false);
                     userItemRepository.save(currentlyEquipped);
                     // Cast needed here too if accessing name or slot
                     logger.info("Unequipped item {} from slot {}", ((Item) currentlyEquipped.getItem()).getName(), slot);
                }
            }


            // Equip the new item
            itemToEquip.setIsEquipped(true);
            userItemRepository.save(itemToEquip);
            // Cast needed here
            logger.info("Equipped item {} to slot {}", ((Item) itemToEquip.getItem()).getName(), slot);

            // TODO: Recalculate character stats if needed immediately after equip

            return ApiResponse.success("Trang bị '" + itemData.getName() + "' thành công!", null);
        } catch (Exception e) {
            logger.error("Lỗi khi trang bị vật phẩm {}: {}", userItemId, e.getMessage(), e);
            return ApiResponse.error("Lỗi máy chủ khi trang bị: " + e.getMessage());
        }
    }

    @PostMapping("/unequip/{userItemId}")
    @ResponseBody
    @Transactional
    public ApiResponse<Void> unequipItem(@PathVariable Integer userItemId) {
        try {
            User user = getCurrentUser();
            UserItem userItem = userItemRepository.findById(userItemId)
                    .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại!"));

            // Authorization check
            if (!userItem.getUser().getUserId().equals(user.getUserId())) {
                return ApiResponse.error("Không có quyền!");
            }

            if (!Boolean.TRUE.equals(userItem.getIsEquipped())) {
                return ApiResponse.error("Vật phẩm chưa được trang bị!");
            }

            userItem.setIsEquipped(false);
            userItemRepository.save(userItem);
            // Cast needed here
            logger.info("Unequipped item {}", ((Item) userItem.getItem()).getName());

            // TODO: Recalculate character stats if needed immediately

            // Cast needed here
            return ApiResponse.success("Gỡ trang bị '" + ((Item) userItem.getItem()).getName() + "' thành công!", null);
        } catch (Exception e) {
            logger.error("Lỗi khi gỡ trang bị vật phẩm {}: {}", userItemId, e.getMessage(), e);
            return ApiResponse.error("Lỗi máy chủ khi gỡ trang bị: " + e.getMessage());
        }
    }

    @PostMapping("/use/{userItemId}")
    @ResponseBody
    @Transactional
    public ApiResponse<Void> useItem(@PathVariable Integer userItemId) {
        try {
            User user = getCurrentUser();
            UserItem userItem = userItemRepository.findById(userItemId)
                    .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại!"));

             // Authorization check
             if (!userItem.getUser().getUserId().equals(user.getUserId())) {
                 return ApiResponse.error("Không có quyền!");
             }

             // *** FIX: Cast userItem.getItem() to Item BEFORE using it ***
             Item item = (Item) userItem.getItem();
             if (item == null || !"potion".equalsIgnoreCase(item.getItemCategory())) {
                 return ApiResponse.error("Vật phẩm này không thể sử dụng!");
             }

             // Logic to use potion (e.g., heal character)
             // Find the character
             var characterOpt = user.getCharacters().stream().findFirst();
             if (characterOpt.isEmpty()) {
                 return ApiResponse.error("Không tìm thấy nhân vật để sử dụng vật phẩm!");
             }
             CharacterGame character = characterOpt.get();

             if (character.getHealth() >= character.getMaxHealth()) {
                 return ApiResponse.error("Máu đã đầy!");
             }

             // *** FIX: Use the 'item' variable which is already casted ***
             int healAmount = item.getGameHeal() != null ? item.getGameHeal() : 0;
             character.setHealth(Math.min(character.getMaxHealth(), character.getHealth() + healAmount));
             // No need to save character here if it's managed by JPA and part of the transaction

             // Decrease item quantity or delete
             userItem.setQuantity(userItem.getQuantity() - 1);
             if (userItem.getQuantity() <= 0) {
                 userItemRepository.delete(userItem);
             } else {
                 userItemRepository.save(userItem);
             }
             // *** FIX: Use the 'item' variable ***
             logger.info("Used potion {}, healed {} HP", item.getName(), healAmount);

             // *** FIX: Use the 'item' variable ***
             return ApiResponse.success("Sử dụng " + item.getName() + " thành công! Hồi " + healAmount + " HP.", null);
        } catch (Exception e) {
            logger.error("Lỗi khi sử dụng vật phẩm {}: {}", userItemId, e.getMessage(), e);
            return ApiResponse.error("Lỗi máy chủ khi sử dụng vật phẩm: " + e.getMessage());
        }
    }
}