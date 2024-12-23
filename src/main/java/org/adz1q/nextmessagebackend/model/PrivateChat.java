package org.adz1q.nextmessagebackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "privateChat")
public class PrivateChat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
}
