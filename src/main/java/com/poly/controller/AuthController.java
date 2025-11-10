package com.poly.controller;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.poly.model.CharacterGame;
import com.poly.model.CharacterGathering;
import com.poly.model.Role;
import com.poly.model.User;
import com.poly.model.Wallet;
import com.poly.repository.CharacterGameRepository;
import com.poly.repository.RoleRepository;
import com.poly.repository.UserRepository;
import com.poly.repository.WalletRepository;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CharacterGameRepository characterGameRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(
            Model model,
            @RequestParam(value = "register_success", required = false) String registerSuccess,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout) {
        
        if (registerSuccess != null) {
            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        if (error != null) {
            model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
        }
        if (logout != null) {
            model.addAttribute("success", "Bạn đã đăng xuất thành công.");
        }
        
        return "pages/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "pages/register";
    }

    @PostMapping("/register")
    @Transactional
    public String doRegister(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String full_name,
            @RequestParam String password,
            @RequestParam String confirm,
            RedirectAttributes redirectAttributes) {

        logger.info("Registration attempt for username: {}", username);

        // Validation
        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu không được để trống!");
            return "redirect:/register";
        }

        if (!password.equals(confirm)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("full_name", full_name);
            return "redirect:/register";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại!");
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("full_name", full_name);
            return "redirect:/register";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("full_name", full_name);
            return "redirect:/register";
        }

        try {
            // Create User
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(full_name);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setIsActivea(true);
            user.setCreatedAt(LocalDateTime.now());

            // Assign Player role
            Role playerRole = roleRepository.findByRoleName("Player")
                    .orElseThrow(() -> new RuntimeException("Role 'Player' không tồn tại trong database!"));
            user.setRole(playerRole);

            User savedUser = userRepository.save(user);
            logger.info("User created successfully: {}", username);

            // Create Wallet
            Wallet wallet = new Wallet();
            wallet.setUser(savedUser);
            walletRepository.save(wallet);
            logger.info("Wallet created for user: {}", username);

            // Create Character
            CharacterGame character = new CharacterGame();
            character.setUser(savedUser);
            character.setName(savedUser.getUsername());

            // Initialize gathering levels
            Set<CharacterGathering> gatheringLevels = new HashSet<>();
            
            CharacterGathering stone = new CharacterGathering();
            stone.setCharacter(character);
            stone.setResourceType("stone");
            gatheringLevels.add(stone);

            CharacterGathering wood = new CharacterGathering();
            wood.setCharacter(character);
            wood.setResourceType("wood");
            gatheringLevels.add(wood);

            CharacterGathering fish = new CharacterGathering();
            fish.setCharacter(character);
            fish.setResourceType("fish");
            gatheringLevels.add(fish);

            CharacterGathering ore = new CharacterGathering();
            ore.setCharacter(character);
            ore.setResourceType("ore");
            gatheringLevels.add(ore);

            character.setGatheringLevels(gatheringLevels);
            characterGameRepository.save(character);
            logger.info("Character created for user: {}", username);

            return "redirect:/login?register_success=true";
            
        } catch (Exception e) {
            logger.error("Error during registration for username {}: {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi đăng ký. Vui lòng thử lại!");
            return "redirect:/register";
        }
    }
}