package com.example.libapp.controller;

import com.example.libapp.entity.User;
import com.example.libapp.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        User user = authService.register(
                body.get("fullName"), body.get("username"), body.get("password"));
        return Map.of("success", true, "message", "Account created for " + user.getFullName());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        User user = authService.login(body.get("username"), body.get("password"));
        return Map.of("success", true, "name", user.getFullName());
    }
}