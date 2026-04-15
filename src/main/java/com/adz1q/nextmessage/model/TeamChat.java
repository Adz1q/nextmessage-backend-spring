package com.adz1q.nextmessage.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "team_chat")
public class TeamChat extends Chat {
    private String name;
    private String profilePictureUrl;
    private int adminId;
}
