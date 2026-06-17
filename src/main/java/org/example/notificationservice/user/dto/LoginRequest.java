package org.example.notificationservice.user.dto;

public record LoginRequest(
        String email,
        String password
) {
}