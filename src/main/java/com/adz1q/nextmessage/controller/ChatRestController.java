package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatRestController {
    private final ChatService chatService;

    @Autowired
    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/get/{chatId}/messages")
    public ResponseEntity<Object> getMessages(
            @PathVariable int chatId,
            @RequestParam int userId,
            @RequestParam int offset,
            @RequestParam int limit
    ) {
        return chatService.getMessages(chatId, userId, offset, limit);
    }

    @GetMapping("/get/{chatId}")
    public ResponseEntity<Object> getChat(
            @PathVariable int chatId,
            @RequestParam int userId
    ) {
        return chatService.getChat(chatId, userId);
    }

    @GetMapping("/get/{firstUserId}/{secondUserId}")
    public ResponseEntity<Object> getPrivateChatByMembers(@PathVariable int firstUserId, @PathVariable int secondUserId) {
        return chatService.getPrivateChatByMembers(firstUserId, secondUserId);
    }

    @GetMapping("/getAll/{userId}")
    public List<ChatService.ChatCard> getChats(@PathVariable int userId) {
        return chatService.getChatsByUserId(userId);
    }

    @GetMapping("/get/{chatId}/members")
    public ResponseEntity<Object> getChatMembers(
            @PathVariable int chatId,
            @RequestParam int userId
    ) {
        return chatService.getChatMembers(chatId, userId);
    }

    @GetMapping("/get/{chatId}/otherMember")
    public ResponseEntity<Object> getOtherPrivateChatMember(
            @PathVariable int chatId,
            @RequestParam int userId
    ) {
        return chatService.getOtherPrivateChatMember(chatId, userId);
    }

    @PostMapping("/create/private")
    public ResponseEntity<Object> createPrivateChat(
            @RequestBody ChatService.PrivateChatRequestDto privateChatRequestDto
    ) {
        return chatService.createPrivateChat(privateChatRequestDto);
    }

    @PostMapping("/create/team")
    public ResponseEntity<Object> createTeamChat(
            @RequestBody ChatService.TeamChatRequestDto teamChatRequestDto
    ) {
        return chatService.createTeamChat(teamChatRequestDto);
    }

    @PostMapping("/change/name")
    public ResponseEntity<Object> changeTeamChatName(
            @RequestBody ChatService.ChangeTeamChatNameRequestDto changeTeamChatNameRequestDto
    ) {
        return chatService.changeTeamChatName(changeTeamChatNameRequestDto);
    }

    @DeleteMapping("/delete/profilePicture")
    public ResponseEntity<Object> deleteTeamChatProfilePicture(
            @RequestBody ChatService.DeleteTeamChatProfilePictureRequestDto deleteTeamChatProfilePictureRequestDto
    ) {
        return chatService.deleteTeamChatProfilePicture(deleteTeamChatProfilePictureRequestDto);
    }

    @PostMapping("/change/admin")
    public ResponseEntity<Object> changeTeamChatAdmin(
            @RequestBody ChatService.ChangeTeamChatAdminRequestDto changeTeamChatAdminRequestDto
    ) {
        return chatService.changeTeamChatAdmin(changeTeamChatAdminRequestDto);
    }

    @PostMapping("/add/member")
    public ResponseEntity<Object> addTeamChatMember(
            @RequestBody ChatService.AddTeamChatMemberRequestDto addTeamChatMemberRequestDto
    ) {
        return chatService.addTeamChatMember(addTeamChatMemberRequestDto);
    }

    @DeleteMapping("/remove/member")
    public ResponseEntity<Object> removeTeamChatMember(
            @RequestBody ChatService.RemoveTeamChatMemberRequestDto removeTeamChatMemberRequestDto
    ) {
        return chatService.removeTeamChatMember(removeTeamChatMemberRequestDto);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteTeamChat(
            @RequestBody ChatService.DeleteTeamChatRequestDto deleteTeamChatRequestDto
    ) {
        return chatService.deleteTeamChat(deleteTeamChatRequestDto);
    }
}