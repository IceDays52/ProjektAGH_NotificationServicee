package org.example.notificationservice.notification;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByReceivedAtDesc(userId);
    }

    @GetMapping("/mail-account/{mailAccountId}")
    public List<Notification> getMailAccountNotifications(@PathVariable Long mailAccountId) {
        return notificationRepository.findByMailAccountIdOrderByReceivedAtDesc(mailAccountId);
    }
}