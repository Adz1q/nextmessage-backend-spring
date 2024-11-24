package org.adz1q.nextmessagebackend.controller;

import org.adz1q.nextmessagebackend.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    // Trzeba będzie dodać tu simpMessagingTemplate.convertAndSendToUser(message.getReceiverTeamName(), "/private", message); bo to wysyła na prywatny kanał i usunac @SendTo
    @MessageMapping("/message")
    @SendTo("/team/public")
    private Message recievePublicMessage(@Payload Message message) {
        return message;
    }

    @MessageMapping("/privateMessage")
    private Message recievePrivateMessage(@Payload Message message) {
        simpMessagingTemplate.convertAndSendToUser(message.getReceiverName(), "/private", message);
        return message;
    }
}