package com.poly.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.poly.dto.BattleStateDTO;
import com.poly.service.BattleService;

@Controller
@RequestMapping("/battle")
public class BattleController {

    private static final Logger logger = LoggerFactory.getLogger(BattleController.class);

    @Autowired
    private BattleService battleService;

    @GetMapping("/{enemyId}")
    public String showBattlePage(@PathVariable Integer enemyId, Model model, RedirectAttributes redirectAttributes) {
        try {
            BattleStateDTO data = battleService.getBattleState(enemyId);
            model.addAttribute("data", data);
            return "battle/index"; // Path to your battle Thymeleaf template
        } catch (Exception e) {
            logger.error("Error getting battle state for enemyId {}: {}", enemyId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Không thể bắt đầu trận đấu: " + e.getMessage());
            return "redirect:/minigame"; // Redirect back if error
        }
    }
}