package org.example.notificationservice.google;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.service.EncryptionService;
import org.example.notificationservice.user.User;
import org.example.notificationservice.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleAccountService {

    private final GoogleAccountRepository googleAccountRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public GoogleAccount saveGoogleAccount(
            Long userId,
            String googleEmail,
            String accessToken,
            String refreshToken,
            LocalDateTime expiresAt
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String normalizedEmail = googleEmail.trim().toLowerCase();

        Optional<GoogleAccount> existing =
                googleAccountRepository.findFirstByUserIdAndGoogleEmailOrderByIdDesc(
                        userId,
                        normalizedEmail
                );

        GoogleAccount account = existing.orElseGet(GoogleAccount::new);

        account.setUser(user);
        account.setGoogleEmail(normalizedEmail);
        account.setEncryptedAccessToken(encryptionService.encrypt(accessToken));

        if (refreshToken != null && !refreshToken.equals("null") && !refreshToken.isBlank()) {
            account.setEncryptedRefreshToken(encryptionService.encrypt(refreshToken));
        }

        account.setExpiresAt(expiresAt);
        account.setActive(true);

        return googleAccountRepository.save(account);
    }

    public GoogleAccount getUserGoogleAccount(Long userId) {
        return googleAccountRepository.findByUserIdAndActiveTrue(userId)
                .orElse(null);
    }

    public void delete(Long id) {
        GoogleAccount account = googleAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Google account not found"));

        account.setActive(false);
        googleAccountRepository.save(account);
    }
}