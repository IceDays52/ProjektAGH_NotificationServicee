package org.example.notificationservice.google;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/google")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class GoogleOAuthController {

    private final GoogleAccountService googleAccountService;
    private final GoogleSyncService googleSyncService;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @GetMapping("/auth-url")
    public Map<String, String> getAuthUrl(@RequestParam Long userId) {
        String scopes = String.join(" ",
                "openid",
                "email",
                "profile",
                "https://www.googleapis.com/auth/calendar.readonly",
                "https://www.googleapis.com/auth/tasks.readonly"
        );

        String url = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", scopes)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", userId)
                .build()
                .toUriString();

        return Map.of("url", url);
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        Long userId = Long.parseLong(state);

        RestTemplate restTemplate = new RestTemplate();

        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map tokenData = tokenResponse.getBody();

        String accessToken = String.valueOf(tokenData.get("access_token"));
        String refreshToken = String.valueOf(tokenData.get("refresh_token"));
        Integer expiresIn = (Integer) tokenData.get("expires_in");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                Map.class
        );

        String googleEmail = String.valueOf(userInfoResponse.getBody().get("email"));

        googleAccountService.saveGoogleAccount(
                userId,
                googleEmail,
                accessToken,
                refreshToken,
                LocalDateTime.now().plusSeconds(expiresIn)
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(java.net.URI.create("http://localhost:5173?google=connected"))
                .build();
    }

    @GetMapping("/user/{userId}")
    public GoogleAccount getGoogleAccount(@PathVariable Long userId) {
        return googleAccountService.getUserGoogleAccount(userId);
    }

    @PostMapping("/sync")
    public String syncGoogle() {
        googleSyncService.syncAllGoogleAccounts();
        return "Google synced";
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        googleAccountService.delete(id);
    }
}