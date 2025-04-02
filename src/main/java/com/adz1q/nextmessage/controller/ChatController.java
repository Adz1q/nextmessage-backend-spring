package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.model.ChatMessage;
import com.adz1q.nextmessage.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendMessage") // This is the endpoint that the frontend will send messages to
    @SendTo("/topic/chat/{chatId}") // This is the topic that the frontend will subscribe to and there will be sent responses
    public ChatMessage sendMessage(ChatMessage chatMessage) throws Exception {
        return chatService.sendMessage(chatMessage);
    }

//    @MessageMapping("/chat.markAsRead")
//    @SendTo("/topic/chat/{chatId}")
    
}