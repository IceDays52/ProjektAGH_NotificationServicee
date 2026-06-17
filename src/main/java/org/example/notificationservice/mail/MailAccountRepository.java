package org.example.notificationservice.mail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MailAccountRepository extends JpaRepository<MailAccount, Long> {

    List<MailAccount> findByActiveTrue();

    List<MailAccount> findByUserIdAndActiveTrue(Long userId);

    Optional<MailAccount> findByUserIdAndGmailAddress(Long userId, String gmailAddress);
}