package com.example.libapp.service;

import com.example.libapp.entity.User;
import com.example.libapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Sign up: validate -> check username is free -> hash password -> save
    public User register(String fullName, String username, String password) {
        if (isBlank(fullName) || isBlank(username) || isBlank(password)) {
            throw new RuntimeException("All fields are required");
        }
        username = username.trim();
        if (username.length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }
        if (password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already taken, try another one");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setUsername(username);
        user.setPassword(hash(password));
        return userRepository.save(user);
    }

    // Login: find user by username -> compare hash of typed password with saved hash
    public User login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new RuntimeException("Enter username and password");
        }
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!user.getPassword().equals(hash(password))) {
            throw new RuntimeException("Invalid username or password");
        }
        return user;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    // SHA-256 turns the password into a fixed scrambled string (one-way)
    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not secure the password");
        }
    }
}