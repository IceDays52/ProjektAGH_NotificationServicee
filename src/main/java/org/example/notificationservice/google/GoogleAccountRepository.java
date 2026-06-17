package org.example.notificationservice.google;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoogleAccountRepository extends JpaRepository<GoogleAccount, Long> {

    List<GoogleAccount> findByActiveTrue();

    Optional<GoogleAccount> findByUserIdAndActiveTrue(Long userId);

    Optional<GoogleAccount> findFirstByUserIdAndGoogleEmailOrderByIdDesc(
            Long userId,
            String googleEmail
    );
}