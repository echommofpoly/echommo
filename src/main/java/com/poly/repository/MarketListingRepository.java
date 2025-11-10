package com.poly.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.poly.model.MarketListing;

@Repository
public interface MarketListingRepository extends JpaRepository<MarketListing, Integer> {

    /**
     * Finds active market listings based on search term, category, and max price.
     * Searches item name and category.
     * Category filter is exact match (case-insensitive).
     */
    @Query("SELECT ml FROM MarketListing ml JOIN ml.item i JOIN ml.seller s " + // Explicit joins
           "WHERE ml.status = 'Active' " +
           "AND (:searchTerm IS NULL OR :searchTerm = '' OR " + // Allow empty search term
           "    LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "    LOWER(i.itemCategory) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:category = 'ALL' OR :category IS NULL OR :category = '' OR LOWER(i.itemCategory) = LOWER(:category)) " + // Allow 'ALL' or empty category
           "AND ml.price <= :maxPrice")
    Page<MarketListing> findActiveListingsWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("category") String category,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // Find all listings by a specific seller's user ID
    List<MarketListing> findBySeller_UserId(Integer userId);

    // Find active listings by a specific seller's user ID
    List<MarketListing> findBySeller_UserIdAndStatus(Integer userId, String status);

    // Find listings for a specific item ID
    List<MarketListing> findByItem_ItemIdAndStatus(Integer itemId, String status);
}