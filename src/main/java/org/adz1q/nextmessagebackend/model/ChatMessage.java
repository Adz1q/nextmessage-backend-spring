package org.adz1q.nextmessagebackend.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private int chatId;
    private int senderId;
    private String content;
    private LocalDateTime date;
}
