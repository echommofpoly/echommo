package com.poly.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.model.User;
import com.poly.security.CustomUserDetails; // Import CustomUserDetails

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Example: Require USER or ADMIN role
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // ** FIX: Get CustomUserDetails first for safer access to the User entity **
        Object principal = authentication.getPrincipal();
        User currentUser = null;

        if (principal instanceof CustomUserDetails) {
            currentUser = ((CustomUserDetails) principal).getUser();
        } else {
            // Handle cases where the principal might not be CustomUserDetails
            // This might indicate an issue or a different authentication setup
            // For now, return unauthorized or handle as appropriate
             return ResponseEntity.status(401).build(); // Or throw an exception
        }

        // You might want to detach the entity or use a DTO to avoid sending sensitive info
        // For simplicity here, we return the User object directly
        return ResponseEntity.ok(currentUser);
    }
}