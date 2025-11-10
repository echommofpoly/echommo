package com.poly.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.poly.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String token);

    @Query("SELECT u FROM User u ORDER BY u.username ASC, u.createdAt DESC")
    List<User> findAllOrderByUsernameAndCreatedAtDesc();

    List<User> findByUsernameContainingIgnoreCaseAndUsernameNot(String keyword, String excludedUsername);
}
