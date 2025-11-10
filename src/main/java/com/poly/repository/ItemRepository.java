package com.poly.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poly.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    // Find an item by its exact name (case-insensitive)
    Optional<Item> findByNameIgnoreCase(String name);

    // Find items by category (case-insensitive)
    List<Item> findByItemCategoryIgnoreCase(String itemCategory);

    // Find items by rarity (case-insensitive)
    List<Item> findByRarityIgnoreCase(String rarity);

    // Find items that are equippable (have a non-null gameSlot)
    List<Item> findByGameSlotIsNotNull();
}