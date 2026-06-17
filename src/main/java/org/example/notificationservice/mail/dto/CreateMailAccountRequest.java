package org.example.notificationservice.mail.dto;

public record CreateMailAccountRequest(
        Long userId,
        String gmailAddress,
        String appPassword
) {
}