package com.poly.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.poly.model.CharacterGame;
import com.poly.model.User;

@Repository
public interface CharacterGameRepository extends JpaRepository<CharacterGame, Integer> {

    // Find a character by user and character name (if multiple characters per user are allowed)
    Optional<CharacterGame> findByUserAndName(User user, String name);

    // Find the first character associated with a user ID, ordered by creation (characterId)
    // Useful if assuming one primary character per user for now.
    @Query("SELECT c FROM CharacterGame c WHERE c.user.userId = :userId ORDER BY c.characterId ASC")
    Optional<CharacterGame> findFirstByUserIdOrderByCharacterIdAsc(@Param("userId") Integer userId);

    // Find all characters for a user
    List<CharacterGame> findByUser_UserId(Integer userId);
}