package org.example.notificationservice.google;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.notificationservice.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "google_accounts")
public class GoogleAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String googleEmail;

    @Column(columnDefinition = "TEXT")
    private String encryptedAccessToken;

    @Column(columnDefinition = "TEXT")
    private String encryptedRefreshToken;

    private LocalDateTime expiresAt;

    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}