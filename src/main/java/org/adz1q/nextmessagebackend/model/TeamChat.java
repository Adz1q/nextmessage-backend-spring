package org.adz1q.nextmessagebackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "teamChat")
public class TeamChat extends Chat {
    private String name;
    private String profilePictureUrl;
    private int adminId;
}