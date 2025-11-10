package com.poly.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poly.model.Friendship;
import com.poly.model.User;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    
    // Lấy danh sách bạn bè/lời mời đã GỬI
    List<Friendship> findByUserAndStatus(User user, String status);

    // Kiểm tra yêu cầu đã tồn tại
    Optional<Friendship> findByUserAndFriendUser(User user, User friendUser);

    // Lấy danh sách lời mời đã NHẬN
    List<Friendship> findByFriendUserAndStatus(User friendUser, String status);
}