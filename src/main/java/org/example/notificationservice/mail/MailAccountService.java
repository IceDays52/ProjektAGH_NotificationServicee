package org.example.notificationservice.mail;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.mail.dto.CreateMailAccountRequest;
import org.example.notificationservice.user.User;
import org.example.notificationservice.user.UserRepository;
import org.springframework.stereotype.Service;
import org.example.notificationservice.service.EncryptionService;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MailAccountService {

    private final MailAccountRepository mailAccountRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public MailAccount addMailAccount(CreateMailAccountRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String cleanedGmail = request.gmailAddress().trim().toLowerCase();
        String cleanedPassword = request.appPassword()
                .replaceAll("\\s+", "")
                .trim();

        String encryptedPassword =
                encryptionService.encrypt(cleanedPassword);

        Optional<MailAccount> existingAccount =
                mailAccountRepository.findByUserIdAndGmailAddress(
                        user.getId(),
                        cleanedGmail
                );

        if (existingAccount.isPresent()) {

            MailAccount account = existingAccount.get();

            account.setAppPassword(encryptedPassword);
            account.setActive(true);

            return mailAccountRepository.save(account);
        }

        MailAccount account = new MailAccount();

        account.setUser(user);
        account.setGmailAddress(cleanedGmail);
        account.setAppPassword(encryptedPassword);
        account.setActive(true);

        return mailAccountRepository.save(account);
    }

    public List<MailAccount> getUserMailAccounts(Long userId) {
        return mailAccountRepository.findByUserIdAndActiveTrue(userId);
    }

    public void deleteMailAccount(Long id) {

        MailAccount account = mailAccountRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Mail account not found"));

        account.setActive(false);

        mailAccountRepository.save(account);
    }
}