package com.poly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model; // <<<--- THÊM IMPORT
import org.springframework.beans.factory.annotation.Autowired; // <<<--- THÊM IMPORT
import com.poly.model.User; // <<<--- THÊM IMPORT
import com.poly.repository.UserRepository; // <<<--- THÊM IMPORT
import org.springframework.security.core.Authentication; // <<<--- THÊM IMPORT
import org.springframework.security.core.context.SecurityContextHolder; // <<<--- THÊM IMPORT
import org.springframework.security.core.userdetails.UsernameNotFoundException; // <<<--- THÊM IMPORT


@Controller
public class RoleController {

    // Inject UserRepository để lấy thông tin User đầy đủ (cho header/sidebar)
    @Autowired
    private UserRepository userRepository;

    // Helper method to get the currently logged-in User entity
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));
    }

    // Mapping for the Admin dashboard page
    @GetMapping("/admin/dashboard")
    public String adminHome(Model model) { // Thêm Model
        User user = getCurrentUser();
        model.addAttribute("currentUser", user); // Thêm user vào model cho header/sidebar
        model.addAttribute("activePage", "admin-dashboard"); // Đặt tên trang active (tùy chọn)
        // Spring Security already ensures only ADMIN can access this via SecurityConfig
        return "admin/admin_home"; // Returns templates/admin/admin_home.html
    }

    // The /player/home mapping is removed because the general home page is handled by HomeController at "/"
    // @GetMapping("/player/home")
    // public String playerHome() {
    //     return "user/home"; // Player or Admin could access this if uncommented and configured
    // }
}