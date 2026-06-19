package org.example.notificationservice.admin.dto;

public record AdminStatsResponse(
        long usersCount,
        long adminsCount,
        long mailAccountsCount,
        long googleAccountsCount,
        long notificationsCount
) {
}