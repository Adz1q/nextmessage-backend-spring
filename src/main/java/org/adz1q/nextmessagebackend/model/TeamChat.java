package org.adz1q.nextmessagebackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "teamChat")
public class TeamChat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int adminId;
    @Column(name = "lastUpdated", nullable = false, updatable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdated;
}