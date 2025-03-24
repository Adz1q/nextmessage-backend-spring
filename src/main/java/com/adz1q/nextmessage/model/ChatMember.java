package com.adz1q.nextmessage.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "chat_member")
public class ChatMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int chatId;
    private int userId;
}
