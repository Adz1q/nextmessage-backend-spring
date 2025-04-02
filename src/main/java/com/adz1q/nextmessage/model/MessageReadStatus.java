package com.adz1q.nextmessage.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "message_read_status")
public class MessageReadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int messageId;
    private int userId;
    private boolean read;
    private LocalDateTime readAt;
}
