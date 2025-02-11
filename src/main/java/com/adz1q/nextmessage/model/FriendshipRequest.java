package com.adz1q.nextmessage.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "friendshipRequest")
public class FriendshipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int senderId;
    private int receiverId;

    @Column(name = "date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime date;
}
