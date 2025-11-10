package com.poly.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.poly.model.CharacterGame;
import com.poly.model.User;
import com.poly.repository.CharacterGameRepository;
import com.poly.repository.UserRepository;
import com.poly.service.CharacterService;

import java.util.Optional; // Import Optional

@Controller
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CharacterGameRepository characterGameRepo;
    @Autowired private CharacterService characterService;

    // Helper method to get the currently logged-in User entity
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        // Fetch user with wallet eagerly if needed often, or rely on lazy loading
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));
    }

    // Display profile page
    @GetMapping("/profile")
    public String showProfile(Model model) {
        User user = getCurrentUser(); // Get the current user
        CharacterGame character = characterGameRepo.findFirstByUserIdOrderByCharacterIdAsc(user.getUserId())
                .orElse(null); // Allow null if no character exists

        // Add user and character to the model
        model.addAttribute("currentUser", user); // Use "currentUser" for consistency with header/sidebar
        model.addAttribute("user", user); // Keep "user" for form binding if needed in profile.html
        model.addAttribute("character", character);
        model.addAttribute("activePage", "profile"); // For sidebar highlighting

        // Add EXP needed for next level
        if (character != null) {
            model.addAttribute("expToNextLevel", characterService.calculateExpForNextLevel(character.getLevel()));
        } else {
             model.addAttribute("expToNextLevel", characterService.calculateExpForNextLevel(1)); // Exp for level 1 if no char
        }

        return "pages/profile"; // Return the profile view
    }

    // Update basic profile info (Full Name, Email)
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName, @RequestParam String email, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        boolean emailExistsError = false; // Flag for email existence check

        // --- Validation ---
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            redirectAttributes.addFlashAttribute("error", "Email không hợp lệ!");
            return "redirect:/profile";
        }

        // Check if email is already taken by ANOTHER user
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        if (existingUserOpt.isPresent() && !existingUserOpt.get().getUserId().equals(user.getUserId())) {
             emailExistsError = true; // Set flag if email belongs to another user
        }

        if (emailExistsError) {
            redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng bởi tài khoản khác!");
            return "redirect:/profile";
        }
        // --- End Validation ---

        // Update user information
        user.setFullName(fullName); // Use correct setter
        user.setEmail(email);

        // Save and add messages
        try {
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
            logger.info("User {} updated profile info", user.getUsername());
        } catch (Exception e) {
             logger.error("Error updating profile for user {}: {}", user.getUsername(), e.getMessage(), e);
             // Use a more specific message if possible, otherwise keep generic
             redirectAttributes.addFlashAttribute("error", "Lỗi CSDL khi cập nhật thông tin.");
        }

        return "redirect:/profile"; // Redirect back to profile page
    }

    // Change password
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();

        // --- Validation ---
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) { // Use correct getter
            redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không đúng!");
            return "redirect:/profile";
        }
        // Add more robust password policy checks if desired (length, complexity)
        if (newPassword == null || newPassword.length() < 3) { // Example: Minimum length 3
             redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 3 ký tự!");
             return "redirect:/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Xác nhận mật khẩu mới không khớp!");
            return "redirect:/profile";
        }
        // --- End Validation ---

        // Update password hash
        try {
            user.setPasswordHash(passwordEncoder.encode(newPassword)); // Use correct setter
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công!");
            logger.info("User {} changed password", user.getUsername());
        } catch (Exception e) {
             logger.error("Error changing password for user {}: {}", user.getUsername(), e.getMessage(), e);
             redirectAttributes.addFlashAttribute("error", "Lỗi CSDL khi đổi mật khẩu.");
        }

        return "redirect:/profile"; // Redirect back to profile page
    }

    // Allocate stat points
    @PostMapping("/profile/allocate-stat")
    public String allocateStat(@RequestParam String statType, RedirectAttributes redirectAttributes) {
         User user = getCurrentUser();
         // Fetch character associated with the user
         CharacterGame character = characterGameRepo.findFirstByUserIdOrderByCharacterIdAsc(user.getUserId())
                                      .orElse(null); // Find character or return null

         if (character == null) {
             redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân vật để nâng cấp!");
             return "redirect:/profile";
         }

         // Attempt to allocate stat point using CharacterService
         try {
             characterService.allocateStatPoint(character.getCharacterId(), statType); // Call service method
             redirectAttributes.addFlashAttribute("message", "Nâng cấp chỉ số '" + statType.toUpperCase() + "' thành công!");
             logger.info("User {} allocated stat point to {}", user.getUsername(), statType);
         } catch (RuntimeException e) { // Catch specific exceptions from service (e.g., "Not enough points")
             logger.warn("Failed allocation attempt by user {}: {}", user.getUsername(), e.getMessage());
             redirectAttributes.addFlashAttribute("error", e.getMessage()); // Show specific error from service
         } catch (Exception e) { // Catch generic errors
              logger.error("Error allocating stat point for user {}: {}", user.getUsername(), e.getMessage(), e);
              redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi nâng cấp chỉ số.");
         }
         return "redirect:/profile"; // Redirect back to profile page
    }
}