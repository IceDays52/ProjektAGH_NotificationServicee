package org.example.notificationservice.auth;

import org.example.notificationservice.user.User;
import org.example.notificationservice.user.UserService;
import org.example.notificationservice.user.dto.AuthResponse;
import org.example.notificationservice.user.dto.LoginRequest;
import org.example.notificationservice.user.dto.RegisterRequest;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}