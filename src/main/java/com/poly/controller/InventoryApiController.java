package com.poly.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.model.Item; // Ensure Item is imported
import com.poly.model.User;
import com.poly.model.UserItem;
import com.poly.repository.UserItemRepository;
import com.poly.repository.UserRepository;

@RestController
@RequestMapping("/api/inventory")
public class InventoryApiController {

    @Autowired
    private UserItemRepository userItemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/items")
    public ResponseEntity<List<Map<String, Object>>> getInventoryItems() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<UserItem> userItems = userItemRepository.findByUser_UserIdAndQuantityGreaterThan(user.getUserId(), 0); // Only get items with quantity > 0

        List<Map<String, Object>> itemsDto = userItems.stream()
                .filter(ui -> ui.getItem() != null) // Ensure item data exists
                .map(ui -> {
                    Map<String, Object> map = new HashMap<>();
                    Item item = (Item) ui.getItem(); // Explicitly cast to Item
                    map.put("userItemId", ui.getUserItemId());
                    map.put("itemId", item.getItemId());
                    map.put("name", item.getName());
                    map.put("description", item.getDescription());
                    map.put("rarity", item.getRarity());
                    map.put("category", item.getItemCategory());
                    map.put("quantity", ui.getQuantity());
                    map.put("basePrice", item.getBasePrice());
                    map.put("imageUrl", item.getImageUrl());
                    map.put("isEquipped", ui.getIsEquipped()); // Include equipped status
                    map.put("gameSlot", item.getGameSlot());   // Include slot for equipping logic
                    map.put("gameAtk", item.getGameAtk());
                    map.put("gameDef", item.getGameDef());
                    map.put("gameHeal", item.getGameHeal());
                    return map;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(itemsDto);
    }
}