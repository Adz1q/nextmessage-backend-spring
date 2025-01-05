package org.adz1q.nextmessagebackend.model;

import jakarta.persistence.*;
import lombok.Data;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int chatId;
    private int senderId;
    private String content;
    private SecretKey secretKey;

    @Column(name = "date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime date;
}
