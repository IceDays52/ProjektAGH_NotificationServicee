package org.example.notificationservice.controllers;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.CreateNotificationRequest;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WhatsappController {

    private final NotificationService notificationService;

    @PostMapping("/api/whatsapp/webhook")
    public Notification receiveWhatsappMessage(
            @RequestBody CreateNotificationRequest request
    ) {
        request.setType("WHATSAPP");
        return notificationService.create(request);
    }
}