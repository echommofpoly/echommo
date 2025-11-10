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

import com.poly.dto.GatheringPageDTO;
import com.poly.service.GatheringService;

@Controller
@RequestMapping("/gathering")
public class GatheringController {

    private static final Logger logger = LoggerFactory.getLogger(GatheringController.class);

    @Autowired
    private GatheringService gatheringService;

    @GetMapping("/{resourceType}")
    public String showGatheringPage(@PathVariable String resourceType, Model model, RedirectAttributes redirectAttributes) {
        // Basic validation for resourceType
        if (!isValidResourceType(resourceType)) {
             redirectAttributes.addFlashAttribute("error", "Loại tài nguyên không hợp lệ: " + resourceType);
             return "redirect:/minigame";
        }

        try {
            GatheringPageDTO data = gatheringService.getGatheringPageData(resourceType);
            model.addAttribute("data", data);
            return "gathering/index"; // Path to your gathering Thymeleaf template
        } catch (Exception e) {
            logger.error("Error getting gathering page data for {}: {}", resourceType, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Không thể tải trang thu thập: " + e.getMessage());
            return "redirect:/minigame";
        }
    }

    // Helper method to validate resource types
    private boolean isValidResourceType(String type) {
        return type != null && (type.equals("stone") || type.equals("wood") || type.equals("fish") || type.equals("ore"));
    }
}