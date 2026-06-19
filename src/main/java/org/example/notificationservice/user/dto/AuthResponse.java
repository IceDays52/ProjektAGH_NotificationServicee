package org.example.notificationservice.user.dto;

public record AuthResponse(
        Long id,
        String email,
        String role
) {
}