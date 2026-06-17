package org.example.notificationservice.controllers;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.service.EmailSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailSyncService emailSyncService;

    @GetMapping("/api/emails/sync")
    public String syncEmails() throws Exception {
        emailSyncService.syncAllActiveAccounts();
        return "Emails synced!";
    }
}