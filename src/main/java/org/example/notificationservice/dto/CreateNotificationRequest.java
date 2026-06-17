package org.example.notificationservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNotificationRequest {

    private String sender;
    private String recipient;
    private String subject;
    private String content;
    private String type;
}