package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model; 

import com.poly.security.CustomUserDetails;
import com.poly.service.FriendshipService;

@Controller
public class HomeController {

    // Phải có Service này
    @Autowired
    private FriendshipService friendshipService;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal CustomUserDetails principal) { 
        
        if (principal != null) {
            String username = principal.getUsername();
            
            // 💡 Cung cấp dữ liệu cho header.html
            model.addAttribute("pendingRequests", friendshipService.getPendingRequests(username));
            model.addAttribute("sentRequests", friendshipService.getSentRequests(username));
            model.addAttribute("friendList", friendshipService.getFriends(username));
            model.addAttribute("currentUsername", username);
        }

        model.addAttribute("activePage", "home"); 

        return "home"; // Trả về view chính của bạn
    }
}