package org.example.notificationservice.mail;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.notificationservice.user.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "mail_accounts")
public class MailAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String gmailAddress;

    @Column(nullable = false)
    private String appPassword;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}