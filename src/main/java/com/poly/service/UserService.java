package com.poly.service;

import java.time.LocalDateTime; // Keep if needed for other methods
import java.util.Optional;    // Keep if needed for other methods
import java.util.List;        // Keep only if using getAllUsers

import org.springframework.beans.factory.annotation.Autowired;
// No longer need BCryptPasswordEncoder here if login/register are removed
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.poly.model.User;
import com.poly.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // PasswordEncoder is not needed here anymore if register/login methods are removed
    // private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /*
     * The register method is redundant.
     * Registration logic is handled in AuthController.
     */
    /*
    public String register(User user) {
        // ... (code removed) ...
    }
    */

    /*
     * The login method is redundant.
     * Login authentication is handled by Spring Security (CustomUserDetailsService).
     * Updating lastLogin can be done via an AuthenticationSuccessHandler if needed.
     */
    /*
    public boolean login(String username, String password) {
         // ... (code removed) ...
    }
    */

    /**
     * Retrieves all users, ordered by username and then creation date descending.
     * NOTE: Requires the method findAllOrderByUsernameAndCreatedAtDesc()
     * to exist in UserRepository.
     * @return A list of all users sorted as specified.
     */
     public List<User> getAllUsers() {
         // Make sure the method exists in UserRepository and uses correct field name 'createdAt'
         return userRepository.findAllOrderByUsernameAndCreatedAtDesc();
     }

    // Add other user-related service methods here if needed...
    // e.g., findUserById, updateUserStatus, deleteUser, etc.

}