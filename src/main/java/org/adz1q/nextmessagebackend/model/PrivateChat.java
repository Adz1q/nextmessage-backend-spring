package org.adz1q.nextmessagebackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "privateChat")
public class PrivateChat extends Chat {
}