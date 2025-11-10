// /EchoMMO/src/main/java/com/poly/dto/EffectiveStatsDTO.java
package com.poly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Sẽ tự tạo getAtk() trả về int, getDef() trả về int,...
@NoArgsConstructor
@AllArgsConstructor
public class EffectiveStatsDTO {
    private int atk;    // Kiểu nguyên thủy int
    private int def;    // Kiểu nguyên thủy int
    private int maxHp;  // Kiểu nguyên thủy int

    // KHÔNG CÓ PHƯƠNG THỨC getAtk() hay getDef() nào được viết thủ công ở đây nữa
}