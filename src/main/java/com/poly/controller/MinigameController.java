package com.poly.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.poly.model.CharacterGame;
import com.poly.model.User;
import com.poly.model.Wallet;
import com.poly.repository.CharacterGameRepository;
import com.poly.repository.WalletRepository;
import com.poly.security.CustomUserDetails;

@Controller
@RequestMapping("/minigame")
public class MinigameController {

    private static final Logger logger = LoggerFactory.getLogger(MinigameController.class);

    @Autowired private CharacterGameRepository characterGameRepository;
    @Autowired private WalletRepository walletRepository;

    @GetMapping
    public String showMinigame(@AuthenticationPrincipal CustomUserDetails userDetails, 
                                Model model, 
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userDetails.getUser();

            // Load character
            CharacterGame character = characterGameRepository
                    .findFirstByUserIdOrderByCharacterIdAsc(user.getUserId())
                    .orElse(null);

            // Load wallet
            Wallet wallet = walletRepository.findByUser_UserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + user.getUserId()));

            user.setWallet(wallet);

            model.addAttribute("user", user);
            model.addAttribute("character", character);

            return "minigame/index";

        } catch (Exception e) {
            logger.error("Error loading minigame page: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Không thể tải trang minigame: " + e.getMessage());
            return "redirect:/";
        }
    }
}