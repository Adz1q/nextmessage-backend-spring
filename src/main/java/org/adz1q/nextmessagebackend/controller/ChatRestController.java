package org.adz1q.nextmessagebackend.controller;

import org.adz1q.nextmessagebackend.model.Message;
import org.adz1q.nextmessagebackend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/db/chat")
public class ChatRestController {
    private final ChatService chatService;

    @Autowired
    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/get/{chatId}/messages")
    public List<Message> getMessages(
            @PathVariable int chatId,
            @RequestParam int userId,
            @RequestParam int offset,
            @RequestParam int limit
    ) {
        return chatService.getMessages(chatId, userId, offset, limit);
    }

    @PostMapping("/create/private")
    public ResponseEntity<Object> createPrivateChat(
            @RequestBody ChatService.PrivateChatRequestDto privateChatRequestDto
    ) {
        return chatService.createPrivateChat(privateChatRequestDto);
    }
}