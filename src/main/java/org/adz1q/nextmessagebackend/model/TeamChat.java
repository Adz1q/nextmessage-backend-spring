package org.adz1q.nextmessagebackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teamChat")
public class TeamChat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int adminId;
}
