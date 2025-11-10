// /EchoMMO/src/main/java/com/poly/model/Enemy.java
package com.poly.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "enemy")
public class Enemy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enemy_id")
    private Integer enemyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "emoji_icon", length = 10)
    private String emojiIcon;

    @Column(name = "max_hp", nullable = false)
    private Integer maxHp;

    @Column(nullable = false)
    private Integer atk;

    @Column(nullable = false)
    private Integer def;

    @Column(name = "reward_exp_min", columnDefinition = "INT DEFAULT 0")
    private Integer rewardExpMin = 0;

    @Column(name = "reward_exp_max", columnDefinition = "INT DEFAULT 0")
    private Integer rewardExpMax = 0;

    @Column(name = "reward_gold_min", columnDefinition = "INT DEFAULT 0")
    private Integer rewardGoldMin = 0;

    @Column(name = "reward_gold_max", columnDefinition = "INT DEFAULT 0")
    private Integer rewardGoldMax = 0;

     @PrePersist
     protected void onPrePersist() {
         if (rewardExpMin == null) rewardExpMin = 0;
         if (rewardExpMax == null) rewardExpMax = 0;
         if (rewardGoldMin == null) rewardGoldMin = 0;
         if (rewardGoldMax == null) rewardGoldMax = 0;
     }
      // Các phương thức getter thủ công đã bị xóa
}