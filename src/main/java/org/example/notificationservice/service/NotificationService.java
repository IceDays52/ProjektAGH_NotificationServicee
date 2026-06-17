package org.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.CreateNotificationRequest;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification create(CreateNotificationRequest request) {

        Notification notification = new Notification();

        notification.setSender(request.getSender());
        notification.setRecipient(request.getRecipient());
        notification.setSubject(request.getSubject());
        notification.setContent(request.getContent());
        notification.setType(request.getType());

        notification.setStatus("NEW");
        notification.setReceivedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }
}