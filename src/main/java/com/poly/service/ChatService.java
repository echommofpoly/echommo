package com.poly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poly.model.Message;
import com.poly.model.User;
import com.poly.repository.MessageRepository;
import com.poly.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private UserRepository userRepo;

    // Lấy lịch sử chat
    @Transactional(readOnly = true)
    public List<Message> getChatHistory(String usernameA, String usernameB) {
        Optional<User> userA = userRepo.findByUsername(usernameA);
        Optional<User> userB = userRepo.findByUsername(usernameB);

        if (userA.isEmpty() || userB.isEmpty()) return List.of();
        
        // FIX: Truyền username trực tiếp vào Repository
        return messageRepo.findConversationByUsernames(usernameA, usernameB);
    }

    // Gửi tin nhắn
    @Transactional
    public boolean sendMessage(String senderUsername, String receiverUsername, String content) {
        Optional<User> senderOpt = userRepo.findByUsername(senderUsername);
        Optional<User> receiverOpt = userRepo.findByUsername(receiverUsername);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty() || content.trim().isEmpty()) return false;

        Message message = Message.builder()
                .sender(senderOpt.get())
                .receiver(receiverOpt.get())
                .content(content.trim())
                .isRead(false)
                .build();

        messageRepo.save(message);
        return true;
    }
}