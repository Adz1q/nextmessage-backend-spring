package com.adz1q.nextmessage.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "friendship_member")
public class FriendshipMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int friendshipId;
    private int userId;
}