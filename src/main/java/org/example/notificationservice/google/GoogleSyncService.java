package org.example.notificationservice.google;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.repository.NotificationRepository;
import org.example.notificationservice.service.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleSyncService {

    private final GoogleAccountRepository googleAccountRepository;
    private final NotificationRepository notificationRepository;
    private final EncryptionService encryptionService;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    public void syncAllGoogleAccounts() {
        List<GoogleAccount> accounts = googleAccountRepository.findByActiveTrue();

        for (GoogleAccount account : accounts) {
            try {
                syncAccount(account);
            } catch (Exception e) {
                System.out.println("Google sync error: " + account.getGoogleEmail());
                e.printStackTrace();
            }
        }
    }

    public void syncAccount(GoogleAccount account) {
        String accessToken = getValidAccessToken(account);

        syncCalendar(account, accessToken);
        syncTasks(account, accessToken);
    }

    private String getValidAccessToken(GoogleAccount account) {
        if (account.getExpiresAt() != null &&
                account.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(1))) {
            return encryptionService.decrypt(account.getEncryptedAccessToken());
        }

        String refreshToken = encryptionService.decrypt(account.getEncryptedRefreshToken());

        RestTemplate restTemplate = new RestTemplate();

        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map data = response.getBody();

        if (data == null) {
            throw new RuntimeException("Google token response is empty");
        }

        String newAccessToken = String.valueOf(data.get("access_token"));
        Integer expiresIn = (Integer) data.get("expires_in");

        account.setEncryptedAccessToken(encryptionService.encrypt(newAccessToken));
        account.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        googleAccountRepository.save(account);

        return newAccessToken;
    }

    private void syncCalendar(GoogleAccount account, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        String url = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/calendar/v3/calendars/primary/events")
                .queryParam("singleEvents", "true")
                .queryParam("orderBy", "startTime")
                .queryParam("timeMin", OffsetDateTime.now().minusDays(1).toInstant().toString())
                .queryParam("timeMax", OffsetDateTime.now().plusDays(30).toInstant().toString())
                .build()
                .encode()
                .toUriString();

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map body = response.getBody();

        if (body == null) {
            return;
        }

        List<Map<String, Object>> items =
                (List<Map<String, Object>>) body.get("items");

        if (items == null) {
            return;
        }

        for (Map<String, Object> event : items) {
            String id = String.valueOf(event.get("id"));
            String messageId = "calendar-" + account.getId() + "-" + id;

            if (notificationRepository.existsByMessageId(messageId)) {
                continue;
            }

            String summary = String.valueOf(
                    event.getOrDefault("summary", "Google Calendar Event")
            );

            Notification notification = new Notification();
            notification.setUser(account.getUser());
            notification.setSender("Google Calendar");
            notification.setRecipient(account.getGoogleEmail());
            notification.setSubject(summary);
            notification.setContent(buildCalendarContent(event));
            notification.setType("CALENDAR");
            notification.setStatus("NEW");
            notification.setReceivedAt(LocalDateTime.now());
            notification.setMessageId(messageId);

            notificationRepository.save(notification);
        }
    }

    private String buildCalendarContent(Map<String, Object> event) {
        Object startObj = event.get("start");
        Object endObj = event.get("end");

        String start = "";
        String end = "";

        if (startObj instanceof Map<?, ?> startMap) {
            Object dateTime = startMap.get("dateTime");
            Object date = startMap.get("date");
            start = String.valueOf(dateTime != null ? dateTime : date);
        }

        if (endObj instanceof Map<?, ?> endMap) {
            Object dateTime = endMap.get("dateTime");
            Object date = endMap.get("date");
            end = String.valueOf(dateTime != null ? dateTime : date);
        }

        String location = String.valueOf(event.getOrDefault("location", ""));
        String description = String.valueOf(event.getOrDefault("description", ""));

        return """
                Wydarzenie z Google Calendar

                Start: %s
                Koniec: %s
                Lokalizacja: %s

                %s
                """.formatted(start, end, location, description);
    }

    private void syncTasks(GoogleAccount account, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> listsResponse = restTemplate.exchange(
                "https://tasks.googleapis.com/tasks/v1/users/@me/lists",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map listsBody = listsResponse.getBody();

        if (listsBody == null) {
            return;
        }

        List<Map<String, Object>> taskLists =
                (List<Map<String, Object>>) listsBody.get("items");

        if (taskLists == null) {
            return;
        }

        for (Map<String, Object> taskList : taskLists) {
            String taskListId = String.valueOf(taskList.get("id"));
            String taskListTitle = String.valueOf(taskList.get("title"));

            String url = UriComponentsBuilder
                    .fromUriString("https://tasks.googleapis.com/tasks/v1/lists/" + taskListId + "/tasks")
                    .queryParam("showCompleted", "false")
                    .queryParam("showHidden", "false")
                    .build()
                    .encode()
                    .toUriString();

            ResponseEntity<Map> tasksResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map tasksBody = tasksResponse.getBody();

            if (tasksBody == null) {
                continue;
            }

            List<Map<String, Object>> tasks =
                    (List<Map<String, Object>>) tasksBody.get("items");

            if (tasks == null) {
                continue;
            }

            for (Map<String, Object> task : tasks) {
                String id = String.valueOf(task.get("id"));
                String messageId = "task-" + account.getId() + "-" + id;

                if (notificationRepository.existsByMessageId(messageId)) {
                    continue;
                }

                String title = String.valueOf(
                        task.getOrDefault("title", "Google Task")
                );

                String notes = String.valueOf(
                        task.getOrDefault("notes", "")
                );

                String due = String.valueOf(
                        task.getOrDefault("due", "")
                );

                Notification notification = new Notification();
                notification.setUser(account.getUser());
                notification.setSender("Google Tasks");
                notification.setRecipient(taskListTitle);
                notification.setSubject(title);
                notification.setContent("""
                        Zadanie z Google Tasks

                        Lista: %s
                        Termin: %s

                        %s
                        """.formatted(taskListTitle, due, notes));
                notification.setType("TASK");
                notification.setStatus("NEW");
                notification.setReceivedAt(LocalDateTime.now());
                notification.setMessageId(messageId);

                notificationRepository.save(notification);
            }
        }
    }
}