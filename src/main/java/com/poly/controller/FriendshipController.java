package com.poly.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.poly.security.CustomUserDetails;
import com.poly.service.FriendshipService;

@Controller
@RequestMapping("/friends")
public class FriendshipController {

    @Autowired
    private FriendshipService service;

    @PostMapping("/add")
    @ResponseBody
    public Map<String,Object> addFriend(@RequestParam String username,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        // Gửi lời mời kết bạn từ người đang đăng nhập đến username được chỉ định
        boolean success = service.sendFriendRequest(principal.getUsername(), username);
        String message = success ? "✅ Đã gửi lời mời!" : "❌ Không thể gửi lời mời.";
        return Map.of("success", success, "message", message);
    }

    @PostMapping("/respond")
    @ResponseBody
    public String respondFriend(@RequestParam int friendshipId,
                                @RequestParam boolean accept) {
        // Chấp nhận (true) hoặc từ chối (false) lời mời
        return service.respondRequest(friendshipId, accept);
    }
    @GetMapping("/panel-content")
    public String getFriendPanelContent(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
        String username = principal.getUsername();
        
        // Tái sử dụng logic lấy dữ liệu (cần @Autowired FriendshipService)
        model.addAttribute("pendingRequests", service.getPendingRequests(username)); 
        model.addAttribute("sentRequests", service.getSentRequests(username));       
        model.addAttribute("friendList", service.getFriends(username));              
        model.addAttribute("currentUsername", username); 
        
        // Trả về Fragment HTML của Friend Panel
        // Đảm bảo bạn đặt tên Fragment này trong header.html:
        // <div id="friendPanelContent" th:fragment="friendPanelContent">...</div>
        return "fragments/header :: friendPanelContent"; 
    }
}