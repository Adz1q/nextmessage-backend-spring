package org.adz1q.nextmessagebackend.model;

import lombok.Data;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private int chatId;
    private int senderId;
    private String content;
    private LocalDateTime date;
    private SecretKey secretKey;
}
