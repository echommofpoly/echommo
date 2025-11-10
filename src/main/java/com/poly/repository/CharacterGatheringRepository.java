package com.poly.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poly.model.CharacterGame;
import com.poly.model.CharacterGathering;

@Repository
public interface CharacterGatheringRepository extends JpaRepository<CharacterGathering, Long> {

    // Find a specific gathering skill level by Character entity and resource type string
    Optional<CharacterGathering> findByCharacterAndResourceType(CharacterGame character, String resourceType);

    // Find a specific gathering skill level by character ID and resource type string
    Optional<CharacterGathering> findByCharacter_CharacterIdAndResourceType(Integer characterId, String resourceType);

    // Find all gathering skill levels for a specific character
    Set<CharacterGathering> findByCharacter_CharacterId(Integer characterId);
}