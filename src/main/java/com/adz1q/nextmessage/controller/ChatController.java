package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.model.ChatMessage;
import com.adz1q.nextmessage.service.ChatService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    @Autowired
    public ChatController(
            SimpMessagingTemplate simpMessagingTemplate,
            ChatService chatService
    ) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.chatService = chatService;
    }

    @Data
    public static class GetChatsByUserIdDTO {
        private int userId;
    }

    @MessageMapping("/chat.sendMessage") // This is the endpoint that the frontend will send messages to (accessing this endpoint will be calling this method)
//    @SendTo("/topic/chat/{chatId}") // This is the topic that the frontend will subscribe to and there will be sent responses // But in this case we couldn't use SendTo() because the topic have to be dynamic, so we use SimpMessagingTemplate instead in "sendMessage()"
    public void sendMessage(ChatMessage chatMessage) throws Exception {
        ChatService.MessageDto messageDTO = chatService.sendMessage(chatMessage);
        simpMessagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getChatId(), messageDTO);
    }

    @MessageMapping("/chat.getChatsByUserId")
    public void getChatsByUserId(GetChatsByUserIdDTO getChatsDTO) {
        List<ChatService.ChatCard> chatCards = chatService.getChatsByUserId(getChatsDTO.userId);
        simpMessagingTemplate.convertAndSend("/topic/user/" + getChatsDTO.getUserId() + "/chats", chatCards);
        System.out.println(chatCards);
    }
}
