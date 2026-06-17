package org.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.google.GoogleSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleScheduler {

    private final GoogleSyncService googleSyncService;

    @Scheduled(fixedRate = 120000, initialDelay = 30000)
    public void syncGoogleAutomatically() {
        googleSyncService.syncAllGoogleAccounts();
    }
}