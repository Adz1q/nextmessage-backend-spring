package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.service.FriendshipService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class FriendshipController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final FriendshipService friendshipService;

    @Autowired
    public FriendshipController(
            SimpMessagingTemplate simpMessagingTemplate,
            FriendshipService friendshipService
    ) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.friendshipService = friendshipService;
    }

    @Data
    public static class GetFriendsByUserIdDTO {
        private int userId;
    }

    @MessageMapping("friend.getFriendsByUserId")
    public void getFriendsByUserId(GetFriendsByUserIdDTO getFriendsByUserIdDTO) {
        List<FriendshipService.Friend> friends = friendshipService.getFriends(getFriendsByUserIdDTO.getUserId());
        simpMessagingTemplate.convertAndSend("/topic/user/" + getFriendsByUserIdDTO.getUserId() + "/friends", friends);
    }
}
