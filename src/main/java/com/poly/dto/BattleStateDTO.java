package com.poly.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.poly.model.CharacterGame;
import com.poly.model.Enemy;
import com.poly.model.Item;
import com.poly.model.UserItem;

import lombok.Data;

@Data
public class BattleStateDTO {
    // Player Info
    private String playerName;
    private Integer playerHp;
    private Integer playerMaxHp;
    private Integer playerAtk;
    private Integer playerDef;
    private String playerIcon = "🧙‍♂️";

    // Enemy Info
    private Integer enemyId;
    private String enemyName;
    private Integer enemyHp;
    private Integer enemyMaxHp;
    private String enemyIcon;

    // Available Potions
    private List<Map<String, Object>> potions;

    public static BattleStateDTO from(CharacterGame c, Enemy e, EffectiveStatsDTO effectiveStats, List<UserItem> availablePotions) {
        BattleStateDTO dto = new BattleStateDTO();

        // Player Stats
        dto.setPlayerName(c.getName());
        dto.setPlayerHp(c.getHealth());
        dto.setPlayerMaxHp(c.getMaxHealth());
        dto.setPlayerAtk(effectiveStats.getAtk());
        dto.setPlayerDef(effectiveStats.getDef());

        // Enemy Stats
        dto.setEnemyId(e.getEnemyId());
        dto.setEnemyName(e.getName());
        dto.setEnemyHp(e.getMaxHp());
        dto.setEnemyMaxHp(e.getMaxHp());
        dto.setEnemyIcon(e.getEmojiIcon());

        // Map potions
        dto.setPotions(availablePotions.stream()
                .filter(ui -> ui.getItem() != null && ui.getQuantity() > 0)
                .map(ui -> {
                    Item item = (Item) ui.getItem();
                    return Map.of(
                            "userItemId", (Object) ui.getUserItemId(),
                            "name", (Object) item.getName(),
                            "heal", (Object) (item.getGameHeal() != null ? item.getGameHeal() : 0),
                            "quantity", (Object) ui.getQuantity()
                    );
                }).collect(Collectors.toList()));

        return dto;
    }
}