package com.poly.service;

import com.poly.dto.*;
import com.poly.model.*;
import com.poly.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MarketplaceService {

    private static final Logger logger = LoggerFactory.getLogger(MarketplaceService.class);

    @Autowired private MarketListingRepository marketListingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UserItemRepository userItemRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private EntityManager entityManager;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Retrieves a paginated list of active market listings based on filters.
     */
    public Page<MarketListingDTO> getActiveListings(String searchTerm, String category,
                                                      BigDecimal maxPrice, Pageable pageable) {
        String effectiveSearchTerm = (searchTerm == null) ? "" : searchTerm.trim();
        String effectiveCategory = (category == null || category.equalsIgnoreCase("ALL")) ? "" : category.trim();
        BigDecimal effectiveMaxPrice = (maxPrice == null || maxPrice.compareTo(BigDecimal.ZERO) < 0)
                                       ? new BigDecimal("999999999999.99") : maxPrice;

        logger.debug("Fetching active listings with filters: search='{}', category='{}', maxPrice={}, pageable={}",
                     effectiveSearchTerm, effectiveCategory, effectiveMaxPrice, pageable);

        Page<MarketListing> listingsPage = marketListingRepository.findActiveListingsWithFilters(
                effectiveSearchTerm, effectiveCategory, effectiveMaxPrice, pageable);

        List<MarketListingDTO> dtos = listingsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, listingsPage.getTotalElements());
    }

    /**
     * Sells an item directly to the system using a stored procedure.
     */
    @Transactional
    public ApiResponse<BigDecimal> sellToSystem(SellToSystemRequest request) {
        User user = getCurrentUser();
        logger.info("User {} attempting to sell item {} (qty: {}) to system", 
                    user.getUsername(), request.getItemId(), request.getQuantity());

        if (request.getItemId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            return ApiResponse.error("Thông tin không hợp lệ.");
        }

        try {
            StoredProcedureQuery query = entityManager
                    .createStoredProcedureQuery("sp_sell_to_system")
                    .registerStoredProcedureParameter("user_id", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("item_id", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("quantity", Integer.class, ParameterMode.IN)
                    .setParameter("user_id", user.getUserId())
                    .setParameter("item_id", request.getItemId())
                    .setParameter("quantity", request.getQuantity());

            query.execute();

            @SuppressWarnings("unchecked")
            List<Object[]> resultList = query.getResultList();
            if (resultList.isEmpty() || resultList.get(0) == null || !(resultList.get(0)[0] instanceof BigDecimal)) {
                 logger.error("Stored procedure sp_sell_to_system did not return expected BigDecimal for user {}", 
                              user.getUsername());
                 return ApiResponse.error("Lỗi: Không nhận được phản hồi giá trị từ hệ thống.");
            }

            BigDecimal amountReceived = (BigDecimal) resultList.get(0)[0];

            logger.info("User {} successfully sold item {} (qty: {}) for {} gold", 
                        user.getUsername(), request.getItemId(), request.getQuantity(), amountReceived);
            return ApiResponse.success("Bán thành công! Nhận được: " + amountReceived + " vàng", amountReceived);

        } catch (PersistenceException | DataAccessException e) {
            String rootErrorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            if (rootErrorMessage != null && rootErrorMessage.contains("Không đủ vật phẩm")) {
                logger.warn("User {} failed to sell item {}: Not enough items", 
                            user.getUsername(), request.getItemId());
                return ApiResponse.error("Không đủ vật phẩm để bán!");
            } else if (rootErrorMessage != null && rootErrorMessage.contains("Vật phẩm không tồn tại")) {
                logger.warn("User {} failed to sell item {}: Item does not exist", 
                            user.getUsername(), request.getItemId());
                return ApiResponse.error("Vật phẩm không tồn tại!");
            }

            logger.error("Database error during sellToSystem for user {}: {}", 
                         user.getUsername(), rootErrorMessage, e);
            return ApiResponse.error("Lỗi CSDL khi bán vật phẩm: " + rootErrorMessage);
        } catch (Exception e) {
            logger.error("Unexpected error during sellToSystem for user {}: {}", 
                         user.getUsername(), e.getMessage(), e);
            return ApiResponse.error("Lỗi không xác định khi bán vật phẩm: " + e.getMessage());
        }
    }

    /**
     * Creates a new market listing (Player or Admin) using a stored procedure.
     */
    @Transactional
    public ApiResponse<Integer> createPlayerListing(CreateListingRequest request) {
        User user = getCurrentUser();
        logger.info("User {} attempting to create listing for item {} (qty: {}, price: {}, type: {})",
                    user.getUsername(), request.getItemId(), request.getQuantity(), 
                    request.getPrice(), request.getListingType());

        if (request.getItemId() == null || request.getQuantity() == null || request.getQuantity() <= 0 ||
            request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return ApiResponse.error("Thông tin không hợp lệ.");
        }

        // FIX: Check Admin role properly using Lombok getter
        boolean isAdminListing = "Admin".equalsIgnoreCase(request.getListingType());
        if (isAdminListing) {
            Role userRole = user.getRole();
            if (userRole == null || !"Admin".equalsIgnoreCase(userRole.getRoleName())) {
                logger.warn("Non-admin user {} attempted to create Admin listing", user.getUsername());
                return ApiResponse.error("Chỉ Admin mới có thể tạo listing loại này!");
            }
        }

        try {
            StoredProcedureQuery query = entityManager
                    .createStoredProcedureQuery("sp_create_player_listing")
                    .registerStoredProcedureParameter("seller_id", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("item_id", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("quantity", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("price", BigDecimal.class, ParameterMode.IN)
                    .setParameter("seller_id", user.getUserId())
                    .setParameter("item_id", request.getItemId())
                    .setParameter("quantity", request.getQuantity())
                    .setParameter("price", request.getPrice());

            query.execute();

            @SuppressWarnings("unchecked")
            List<Object[]> resultList = query.getResultList();
            if (resultList.isEmpty() || resultList.get(0) == null || !(resultList.get(0)[0] instanceof Integer)) {
                logger.error("Stored procedure sp_create_player_listing did not return expected Integer listing ID for user {}", 
                             user.getUsername());
                return ApiResponse.error("Lỗi: Không nhận được ID listing từ hệ thống.");
            }
            Integer listingId = (Integer) resultList.get(0)[0];

            if (isAdminListing) {
                Optional<MarketListing> listingOpt = marketListingRepository.findById(listingId);
                if (listingOpt.isPresent()) {
                    MarketListing listing = listingOpt.get();
                    listing.setPriceType("Admin");
                    listing.setListingType("Fixed");
                    marketListingRepository.save(listing);
                    logger.info("Admin listing {} created and updated by user {}", listingId, user.getUsername());
                } else {
                    logger.error("Could not find newly created Admin listing {} to update price_type", listingId);
                }
            } else {
                logger.info("Player listing {} created by user {}", listingId, user.getUsername());
            }

            return ApiResponse.success("Đăng bán thành công!", listingId);

        } catch (PersistenceException | DataAccessException e) {
            String rootErrorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            if (rootErrorMessage != null && rootErrorMessage.contains("Không đủ vật phẩm")) {
                logger.warn("User {} failed to create listing for item {}: Not enough items", 
                            user.getUsername(), request.getItemId());
                return ApiResponse.error("Không đủ vật phẩm để đăng bán!");
            }
            logger.error("Database error during createPlayerListing for user {}: {}", 
                         user.getUsername(), rootErrorMessage, e);
            return ApiResponse.error("Lỗi CSDL khi đăng bán: " + rootErrorMessage);
        } catch (Exception e) {
            logger.error("Unexpected error during createPlayerListing for user {}: {}", 
                         user.getUsername(), e.getMessage(), e);
            return ApiResponse.error("Lỗi không xác định khi đăng bán: " + e.getMessage());
        }
    }

    /**
     * Cancels an active market listing using a stored procedure.
     */
    @Transactional
    public ApiResponse<Void> cancelListing(Integer listingId) {
        User user = getCurrentUser();
        logger.info("User {} attempting to cancel listing {}", user.getUsername(), listingId);

        if (listingId == null) {
            return ApiResponse.error("ID Listing không hợp lệ.");
        }

        try {
            StoredProcedureQuery query = entityManager
                    .createStoredProcedureQuery("sp_cancel_listing")
                    .registerStoredProcedureParameter("listing_id", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("user_id", Integer.class, ParameterMode.IN)
                    .setParameter("listing_id", listingId)
                    .setParameter("user_id", user.getUserId());

            query.execute();

            logger.info("User {} successfully cancelled listing {}", user.getUsername(), listingId);
            return ApiResponse.success("Hủy đăng bán thành công!", null);

        } catch (PersistenceException | DataAccessException e) {
            String rootErrorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            if (rootErrorMessage != null && (rootErrorMessage.contains("không tồn tại") || 
                                             rootErrorMessage.contains("đã xử lý"))) {
                logger.warn("User {} failed to cancel listing {}: Listing not found or already processed", 
                            user.getUsername(), listingId);
                return ApiResponse.error("Listing không tồn tại hoặc đã được xử lý!");
            } else if (rootErrorMessage != null && rootErrorMessage.contains("không có quyền hủy")) {
                logger.warn("User {} failed to cancel listing {}: Permission denied", 
                            user.getUsername(), listingId);
                return ApiResponse.error("Bạn không có quyền hủy đăng bán này!");
            }
            logger.error("Database error during cancelListing for user {}: {}", 
                         user.getUsername(), rootErrorMessage, e);
            return ApiResponse.error("Lỗi CSDL khi hủy đăng bán: " + rootErrorMessage);
        } catch (Exception e) {
            logger.error("Unexpected error during cancelListing for user {}: {}", 
                         user.getUsername(), e.getMessage(), e);
            return ApiResponse.error("Lỗi không xác định khi hủy đăng bán: " + e.getMessage());
        }
    }

    /**
     * Buys an item from a market listing using a stored procedure.
     */
    @Transactional
    public ApiResponse<Void> buyItem(BuyItemRequest request) {
        User user = getCurrentUser();
        Integer listingId = request.getListingId();
        logger.info("User {} attempting to buy listing {}", user.getUsername(), listingId);

        if (listingId == null) {
            return ApiResponse.error("ID Listing không hợp lệ.");
        }

        try {
            StoredProcedureQuery query = entityManager
                    .createStoredProcedureQuery("sp_buy_from_market")
                    .registerStoredProcedureParameter("buyer_id", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("listing_id", Integer.class, ParameterMode.IN)
                    .setParameter("buyer_id", user.getUserId())
                    .setParameter("listing_id", listingId);

            query.execute();

            logger.info("User {} successfully bought listing {}", user.getUsername(), listingId);
            return ApiResponse.success("Mua vật phẩm thành công!", null);

        } catch (PersistenceException | DataAccessException e) {
            String rootErrorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            if (rootErrorMessage != null && (rootErrorMessage.contains("không tồn tại") || 
                                             rootErrorMessage.contains("đã bán"))) {
                logger.warn("User {} failed to buy listing {}: Listing not found or already sold", 
                            user.getUsername(), listingId);
                return ApiResponse.error("Listing không tồn tại hoặc đã bán!");
            } else if (rootErrorMessage != null && 
                       rootErrorMessage.contains("Không thể mua vật phẩm của chính mình")) {
                logger.warn("User {} failed to buy listing {}: Attempted self-purchase", 
                            user.getUsername(), listingId);
                return ApiResponse.error("Không thể mua vật phẩm của chính mình!");
            } else if (rootErrorMessage != null && rootErrorMessage.contains("Số dư không đủ")) {
                logger.warn("User {} failed to buy listing {}: Insufficient balance", 
                            user.getUsername(), listingId);
                return ApiResponse.error("Số dư không đủ để mua vật phẩm này!");
            }
            logger.error("Database error during buyItem for user {}: {}", 
                         user.getUsername(), rootErrorMessage, e);
            return ApiResponse.error("Lỗi CSDL khi mua vật phẩm: " + rootErrorMessage);
        } catch (Exception e) {
            logger.error("Unexpected error during buyItem for user {}: {}", 
                         user.getUsername(), e.getMessage(), e);
            return ApiResponse.error("Lỗi không xác định khi mua vật phẩm: " + e.getMessage());
        }
    }

    /**
     * Retrieves all active listings created by the current user.
     */
    public List<MarketListingDTO> getMyListings() {
        User user = getCurrentUser();
        logger.debug("Fetching active listings for user {}", user.getUsername());
        try {
            List<MarketListing> listings = marketListingRepository.findBySeller_UserIdAndStatus(
                    user.getUserId(), "Active");
            return listings.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching listings for user {}: {}", user.getUsername(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Converts a MarketListing entity to its DTO representation.
     */
    private MarketListingDTO convertToDTO(MarketListing listing) {
        if (listing == null) {
            return null;
        }
        MarketListingDTO dto = new MarketListingDTO();
        dto.setListingId(listing.getListingId());
        dto.setQuantity(listing.getQuantity());
        dto.setPrice(listing.getPrice());
        dto.setListingType(listing.getListingType());
        dto.setPriceType(listing.getPriceType());
        dto.setStatus(listing.getStatus());
        dto.setCreatedAt(listing.getCreatedAt());

        if (listing.getSeller() != null) {
            dto.setSellerId(listing.getSeller().getUserId());
            dto.setSellerName(listing.getSeller().getUsername());
        }

        if (listing.getItem() != null) {
            Item item = listing.getItem();
            dto.setItemId(item.getItemId());
            dto.setItemName(item.getName());
            dto.setItemDescription(item.getDescription());
            dto.setItemRarity(item.getRarity());
            dto.setItemType(item.getItemType());
            dto.setItemCategory(item.getItemCategory());
            dto.setImageUrl(item.getImageUrl());
            dto.setGameAtk(item.getGameAtk());
            dto.setGameDef(item.getGameDef());
            dto.setGameHeal(item.getGameHeal());
            dto.setGameSlot(item.getGameSlot());
        }

        return dto;
    }
}