package com.poly.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poly.model.User;
import com.poly.model.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    // Find a wallet by the User entity
    Optional<Wallet> findByUser(User user);

    // Find a wallet by the user's ID
    Optional<Wallet> findByUser_UserId(Integer userId);
}