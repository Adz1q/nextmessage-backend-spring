package org.adz1q.nextmessagebackend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.adz1q.nextmessagebackend.enums.FriendshipRequestStatus;

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
    private FriendshipRequestStatus status;
    @Column(name = "date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime date;
}
