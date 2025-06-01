package com.adz1q.nextmessage.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String username;
    private String email;
    private String password;

    @Column(name = "profilePictureUrl", nullable = false, updatable = true, columnDefinition = "VARCHAR(255) DEFAULT 'https://i.imgur.com'")
    private String profilePictureUrl;

    @Column(name = "date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime date;

    @Column(name = "allowMessagesFromNonFriends", nullable = false, updatable = true, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean allowMessagesFromNonFriends;
}
