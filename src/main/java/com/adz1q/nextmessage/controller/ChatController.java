package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.model.ChatMessage;
import com.adz1q.nextmessage.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    @Autowired
    public ChatController(SimpMessagingTemplate simpMessagingTemplate, ChatService chatService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendMessage")
    // This is the endpoint that the frontend will send messages to (accessing this endpoint will be calling this method)
//    @SendTo("/topic/chat/{chatId}")
    // This is the topic that the frontend will subscribe to and there will be sent responses
    // But in this case we couldn't use SendTo() because the topic have to be dynamic, we use SimpMessagingTemplate instead in "sendMessage()"
    public void sendMessage(ChatMessage chatMessage) throws Exception {
        ChatService.MessageDto messageDto = chatService.sendMessage(chatMessage);
        simpMessagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getChatId(), messageDto);
    }

//    @MessageMapping("/chat.markAsRead")
//    @SendTo("/topic/chat/{chatId}")
    
}
