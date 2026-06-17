package org.example.notificationservice.repository;

import org.example.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByMessageId(String messageId);

    List<Notification> findByUserIdOrderByReceivedAtDesc(Long userId);

    List<Notification> findByMailAccountIdOrderByReceivedAtDesc(Long mailAccountId);
}