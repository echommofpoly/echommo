package com.poly.service;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.poly.model.Friendship;
import com.poly.model.User;
import com.poly.repository.FriendshipRepository; 
import com.poly.repository.UserRepository;      
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Hằng số trạng thái
final class FriendshipStatus {
    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";
}

@Service
public class FriendshipService {

    @Autowired 
    private FriendshipRepository friendshipRepo;

    @Autowired 
    private UserRepository userRepo; 

    @Transactional
    public boolean sendFriendRequest(String username, String friendUsername) {
        if (username.equalsIgnoreCase(friendUsername)) return false; 

        Optional<User> userOpt = userRepo.findByUsername(username);
        Optional<User> friendOpt = userRepo.findByUsername(friendUsername);

        if (userOpt.isEmpty() || friendOpt.isEmpty()) return false; 
        
        User user = userOpt.get();
        User friend = friendOpt.get();

        // Kiểm tra xem đã tồn tại yêu cầu từ user -> friend hoặc friend -> user chưa
        if (friendshipRepo.findByUserAndFriendUser(user, friend).isPresent() ||
            friendshipRepo.findByUserAndFriendUser(friend, user).isPresent()) { 
            return false;
        }

        Friendship f = Friendship.builder().user(user).friendUser(friend).status(FriendshipStatus.PENDING).build();
        friendshipRepo.save(f);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Friendship> getFriends(String username) {
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return List.of(); 
        
        List<Friendship> friends = new ArrayList<>();
        // Lấy các bản ghi nơi người dùng là người gửi (user) và đã accepted
        friends.addAll(friendshipRepo.findByUserAndStatus(user, FriendshipStatus.ACCEPTED));
        // Lấy các bản ghi nơi người dùng là người nhận (friendUser) và đã accepted
        friends.addAll(friendshipRepo.findByFriendUserAndStatus(user, FriendshipStatus.ACCEPTED));
        
        return friends;
    }

    @Transactional(readOnly = true)
    public List<Friendship> getPendingRequests(String username) {
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return List.of(); 
        
        // Lời mời đã NHẬN (user là người nhận/friendUser)
        return friendshipRepo.findByFriendUserAndStatus(user, FriendshipStatus.PENDING);
    }
    
    @Transactional(readOnly = true)
    public List<Friendship> getSentRequests(String username) {
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return List.of(); 
        
        // Lời mời đã GỬI (user là người gửi/user)
        return friendshipRepo.findByUserAndStatus(user, FriendshipStatus.PENDING);
    }

    @Transactional
    public String respondRequest(Integer friendshipId, boolean accept) {
        Optional<Friendship> friendshipOpt = friendshipRepo.findById(friendshipId);
        
        if (friendshipOpt.isEmpty()) {
             return "❌ Lỗi: Không tìm thấy yêu cầu bạn bè.";
        }

        Friendship f = friendshipOpt.get();
        f.setStatus(accept ? FriendshipStatus.ACCEPTED : FriendshipStatus.REJECTED);
        friendshipRepo.save(f);
        return accept ? "Đã chấp nhận" : "Đã từ chối";
    }
}