package org.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final EmailSyncService emailSyncService;

    @Scheduled(fixedRate = 120000)
    public void autoSync() {

        try {
            System.out.println("=== AUTO SYNC START ===");

            emailSyncService.syncAllActiveAccounts();

            System.out.println("=== AUTO SYNC END ===");

        } catch (Exception e) {
            System.out.println("AUTO SYNC ERROR");
            e.printStackTrace();
        }
    }
}