package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.dto.ApiResponse;
import com.poly.dto.MinigameAdventureResponse;
import com.poly.dto.MinigameDataDTO;
import com.poly.service.MinigameService;

@RestController
@RequestMapping("/api/minigame")
public class MinigameApiController {

    @Autowired
    private MinigameService minigameService;

    /**
     * API for the "Adventure" action
     */
    @PostMapping("/adventure")
    public ApiResponse<MinigameAdventureResponse> adventure() {
        try {
            return minigameService.doAdventure();
        } catch (Exception e) {
            // Log exception
            return ApiResponse.error("Lỗi khi phiêu lưu: " + e.getMessage());
        }
    }

    /**
     * API for the "Rest" action
     */
    @PostMapping("/rest")
    public ApiResponse<MinigameDataDTO> rest() {
        try {
            return minigameService.doRest();
        } catch (Exception e) {
            // Log exception
            return ApiResponse.error("Lỗi khi nghỉ ngơi: " + e.getMessage());
        }
    }
}