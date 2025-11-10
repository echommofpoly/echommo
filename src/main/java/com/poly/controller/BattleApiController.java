package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poly.dto.ApiResponse;
import com.poly.dto.BattleActionResponseDTO;
import com.poly.service.BattleService;

@RestController
@RequestMapping("/api/battle")
public class BattleApiController {

    @Autowired
    private BattleService battleService;

    @PostMapping("/attack")
    public ApiResponse<BattleActionResponseDTO> attack(
            @RequestParam Integer enemyId,
            @RequestParam Integer enemyHp) { // Current enemy HP from frontend
        try {
            return battleService.doAttack(enemyId, enemyHp);
        } catch (Exception e) {
            // Log the exception e.g., using SLF4J logger
            return ApiResponse.error("Lỗi khi tấn công: " + e.getMessage());
        }
    }

    @PostMapping("/use-item")
    public ApiResponse<BattleActionResponseDTO> useItem(
            @RequestParam Integer userItemId,
            @RequestParam Integer enemyId,
            @RequestParam Integer enemyHp) { // Current enemy HP from frontend
         try {
             return battleService.doUseItem(userItemId, enemyId, enemyHp);
         } catch (Exception e) {
             // Log the exception
             return ApiResponse.error("Lỗi khi dùng vật phẩm: " + e.getMessage());
         }
    }

    @PostMapping("/flee")
    public ApiResponse<BattleActionResponseDTO> flee() {
        // Simple flee response, no state change needed on server for this basic version
        BattleActionResponseDTO response = new BattleActionResponseDTO();
        response.setBattleStatus(3); // 3 = Fled
        response.addLog("Bạn đã bỏ chạy!");
        return ApiResponse.success("Bỏ chạy thành công", response);
    }
}