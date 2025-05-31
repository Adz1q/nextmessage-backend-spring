package com.adz1q.nextmessage.model;

import lombok.Data;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private int chatId;
    private int senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime date;
    private SecretKey secretKey;
}
