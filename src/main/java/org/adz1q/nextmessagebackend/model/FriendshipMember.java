package org.adz1q.nextmessagebackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "friendshipMember")
public class FriendshipMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int friendshipId;
    private int userId;
}