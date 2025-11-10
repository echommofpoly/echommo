package com.poly.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poly.model.UserItem;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Integer> {

    List<UserItem> findByUser_UserId(Integer userId);
    
    List<UserItem> findByUser_UserIdAndQuantityGreaterThan(Integer userId, int quantity);
    
    Optional<UserItem> findByUser_UserIdAndItem_ItemId(Integer userId, Integer itemId);
    
    List<UserItem> findByUser_UserIdAndIsEquippedTrue(Integer userId);
    
    Optional<UserItem> findByUser_UserIdAndItem_GameSlotAndIsEquippedTrue(Integer userId, String gameSlot);
    
    List<UserItem> findByUser_UserIdAndItem_ItemCategoryAndQuantityGreaterThan(Integer userId, String category, int quantity);
}