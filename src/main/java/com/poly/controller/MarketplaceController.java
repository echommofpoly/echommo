package com.poly.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
// Removed RedirectAttributes as it's not used in this version
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.poly.dto.ApiResponse;
import com.poly.dto.BuyItemRequest;
import com.poly.dto.CreateListingRequest;
import com.poly.dto.MarketListingDTO;
import com.poly.dto.SellToSystemRequest;
import com.poly.model.User; // Import User
import com.poly.repository.UserRepository; // Import UserRepository
import com.poly.service.MarketplaceService;

@Controller
@RequestMapping("/marketplace")
public class MarketplaceController {

    @Autowired private MarketplaceService marketplaceService;
    @Autowired private UserRepository userRepository; // Inject UserRepository

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    // Display marketplace page
    @GetMapping
    public String showMarketplace(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "ALL") String category, // Use "ALL" or empty string for no category filter
            @RequestParam(required = false) BigDecimal maxPrice, // Allow null maxPrice
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        // Provide a very large default maxPrice if none is given
        BigDecimal effectiveMaxPrice = (maxPrice == null) ? new BigDecimal("999999999999.99") : maxPrice;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MarketListingDTO> listings = marketplaceService.getActiveListings(
                search, category, effectiveMaxPrice, pageable);

        model.addAttribute("listings", listings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", listings.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("maxPrice", maxPrice); // Pass original maxPrice (or null) back to view
        model.addAttribute("user", getCurrentUser()); // Pass user for wallet display

        return "marketplace/index";
    }

    // Display user's own listings page
    @GetMapping("/my-listings")
    public String myListings(Model model) {
        List<MarketListingDTO> listings = marketplaceService.getMyListings();
        model.addAttribute("listings", listings);
        model.addAttribute("user", getCurrentUser()); // Pass user for wallet display
        return "marketplace/my-listings";
    }

    // Display create listing form page
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", getCurrentUser()); // Pass user for wallet display
        // Add any other necessary model attributes for the form
        return "marketplace/create-listing";
    }

    // API: Create a listing
    @PostMapping("/create")
    @ResponseBody // Indicates the return value should be bound to the web response body
    public ApiResponse<Integer> createListing(@RequestBody CreateListingRequest request) {
        // Validation can be added here before calling the service
        return marketplaceService.createPlayerListing(request);
    }

    // API: Cancel a listing
    @PostMapping("/cancel/{listingId}")
    @ResponseBody
    public ApiResponse<Void> cancelListing(@PathVariable Integer listingId) {
        return marketplaceService.cancelListing(listingId);
    }

    // API: Buy an item from a listing
    @PostMapping("/buy")
    @ResponseBody
    public ApiResponse<Void> buyItem(@RequestBody BuyItemRequest request) {
        return marketplaceService.buyItem(request);
    }

    // API: Sell an item directly to the system
    @PostMapping("/sell-to-system")
    @ResponseBody
    public ApiResponse<BigDecimal> sellToSystem(@RequestBody SellToSystemRequest request) {
        return marketplaceService.sellToSystem(request);
    }
}