package com.adz1q.nextmessage.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "team_chat")
public class TeamChat extends Chat {
    private String name;
    private String profilePictureUrl;
    private int adminId;
}