package org.example.notificationservice.mail;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.mail.dto.CreateMailAccountRequest;
import org.example.notificationservice.service.EmailSyncService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/mail-accounts")
@RequiredArgsConstructor
public class MailAccountController {

    private final MailAccountService mailAccountService;
    private final EmailSyncService emailSyncService;

    @PostMapping
    public MailAccount addMailAccount(@RequestBody CreateMailAccountRequest request) {
        return mailAccountService.addMailAccount(request);
    }

    @GetMapping("/user/{userId}")
    public List<MailAccount> getUserMailAccounts(@PathVariable Long userId) {
        return mailAccountService.getUserMailAccounts(userId);
    }

    @PostMapping("/sync")
    public String syncAllAccounts() {
        emailSyncService.syncAllActiveAccounts();
        return "Emails synced";
    }

    @DeleteMapping("/{id}")
    public void deleteMailAccount(@PathVariable Long id) {
        mailAccountService.deleteMailAccount(id);
    }
}