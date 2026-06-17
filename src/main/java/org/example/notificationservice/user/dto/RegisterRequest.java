package org.example.notificationservice.user.dto;

public record RegisterRequest(
        String email,
        String password
) {
}