package org.example.notificationservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.notificationservice.mail.MailAccount;
import org.example.notificationservice.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String recipient;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String type;
    private String status;
    private LocalDateTime receivedAt;

    @Column(unique = true)
    private String messageId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "mail_account_id")
    private MailAccount mailAccount;
}