package org.adz1q.nextmessagebackend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.adz1q.nextmessagebackend.enums.MessageStatus;

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
    private MessageStatus status;

    @Column(name = "date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime date;
}
