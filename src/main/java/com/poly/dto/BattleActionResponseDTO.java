package com.poly.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BattleActionResponseDTO {
    private List<String> log = new ArrayList<>();
    private int playerHp;
    private int enemyHp;
    private int battleStatus = 0; // 0 = Ongoing, 1 = Won, 2 = Lost, 3 = Fled
    private Integer rewardExp;
    private BigDecimal rewardGold;
    private Integer updatedPotionQty;
    private Integer userItemId;

    public void addLog(String message) {
        if (message != null && !message.isBlank()) {
            this.log.add(message);
        }
    }
}