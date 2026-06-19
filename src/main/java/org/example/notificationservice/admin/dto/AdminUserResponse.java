package org.example.notificationservice.admin.dto;

public record AdminUserResponse(
        Long id,
        String email,
        String role
) {
}