package org.example.notificationservice.service;

import jakarta.mail.*;
import lombok.RequiredArgsConstructor;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.mail.MailAccount;
import org.example.notificationservice.mail.MailAccountRepository;
import org.example.notificationservice.repository.NotificationRepository;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailSyncService {

    private final NotificationRepository notificationRepository;
    private final MailAccountRepository mailAccountRepository;
    private final EncryptionService encryptionService;

    public void syncAllActiveAccounts() {
        List<MailAccount> accounts = mailAccountRepository.findByActiveTrue();

        for (MailAccount account : accounts) {
            try {
                syncEmailsForAccount(account);
            } catch (Exception e) {
                System.out.println("Błąd synchronizacji konta: " + account.getGmailAddress());
                System.out.println(e.getMessage());
            }
        }
    }

    public void syncEmailsForAccount(MailAccount account) throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(properties);
        Store store = session.getStore("imaps");

        try {
            String decryptedAppPassword = encryptionService.decrypt(account.getAppPassword());

            store.connect(
                    "imap.gmail.com",
                    account.getGmailAddress(),
                    decryptedAppPassword
            );

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int count = inbox.getMessageCount();

            if (count == 0) {
                inbox.close(false);
                return;
            }

            int start = Math.max(1, count - 10);
            Message[] messages = inbox.getMessages(start, count);

            for (Message message : messages) {
                String messageId = getMessageId(message);

                if (messageId != null && notificationRepository.existsByMessageId(messageId)) {
                    continue;
                }

                try {
                    Notification notification = new Notification();

                    notification.setSender(
                            message.getFrom() != null && message.getFrom().length > 0
                                    ? message.getFrom()[0].toString()
                                    : "Unknown"
                    );

                    notification.setRecipient(account.getGmailAddress());
                    notification.setSubject(message.getSubject());
                    notification.setContent(extractTextFromMessage(message));
                    notification.setType("EMAIL");
                    notification.setStatus("NEW");
                    notification.setReceivedAt(LocalDateTime.now());
                    notification.setMessageId(messageId);
                    notification.setUser(account.getUser());
                    notification.setMailAccount(account);

                    notificationRepository.save(notification);

                } catch (Exception e) {
                    System.out.println("Pominięto maila: " + messageId);
                    System.out.println(e.getMessage());
                }
            }

            inbox.close(false);

        } finally {
            if (store.isConnected()) {
                store.close();
            }
        }
    }

    private String getMessageId(Message message) throws MessagingException {
        String[] headers = message.getHeader("Message-ID");
        return headers != null && headers.length > 0 ? headers[0] : null;
    }

    private String extractTextFromMessage(Message message) throws Exception {
        Object content = message.getContent();

        if (content instanceof String text) {
            return message.isMimeType("text/html")
                    ? Jsoup.parse(text).text()
                    : text;
        }

        if (content instanceof Multipart multipart) {
            return extractTextFromMultipart(multipart);
        }

        return "";
    }

    private String extractTextFromMultipart(Multipart multipart) throws Exception {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent()).append("\n");
            } else if (bodyPart.isMimeType("text/html")) {
                String html = String.valueOf(bodyPart.getContent());
                result.append(Jsoup.parse(html).text()).append("\n");
            } else if (bodyPart.getContent() instanceof Multipart nestedMultipart) {
                result.append(extractTextFromMultipart(nestedMultipart));
            }
        }

        return result.toString().trim();
    }
}