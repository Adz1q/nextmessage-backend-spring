package org.adz1q.nextmessagebackend.model;

import lombok.Data;
import org.adz1q.nextmessagebackend.enums.Status;

@Data
public class Message {
    private String senderName;
    private String receiverName;
    private String message;
    private String date;
    private Status status;
}
