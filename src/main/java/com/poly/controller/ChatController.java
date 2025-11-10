package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.poly.model.Message;
import com.poly.security.CustomUserDetails;
import com.poly.service.ChatService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // Lấy lịch sử chat
    @GetMapping("/history/{friendUsername}")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable String friendUsername,
                                                         @AuthenticationPrincipal CustomUserDetails principal) {
        String currentUsername = principal.getUsername();
        List<Message> history = chatService.getChatHistory(currentUsername, friendUsername);
        return ResponseEntity.ok(history);
    }

    // Gửi tin nhắn
    @PostMapping("/send")
    public ResponseEntity<Map<String, Boolean>> sendMessage(@RequestParam String receiverUsername,
                                                          @RequestParam String content,
                                                          @AuthenticationPrincipal CustomUserDetails principal) {
        
        String senderUsername = principal.getUsername();
        boolean success = chatService.sendMessage(senderUsername, receiverUsername, content);
        
        return ResponseEntity.ok(Map.of("success", success));
    }
}