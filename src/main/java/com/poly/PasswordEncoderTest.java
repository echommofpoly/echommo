package com.poly;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        // Create an instance of the BCrypt password encoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Encode the password "123"
        String hashedPassword = encoder.encode("123");

        // Print the generated hash to the console
        System.out.println(hashedPassword);

        // --- Explanatory Comments ---
        // Output will look something like: $2a$10$XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // The exact hash will be different every time you run this code due to BCrypt's salting mechanism.
        // Copy one of the generated hashes (including the $2a$10$ part)
        // and manually insert it into the 'password_hash' column for your initial 'Admin' user in the 'user' table.
    }
}