package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.model.FriendshipRequest;
import com.adz1q.nextmessage.service.FriendshipRequestService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class FriendshipRequestController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final FriendshipRequestService friendshipRequestService;

    @Autowired
    public FriendshipRequestController(
            SimpMessagingTemplate simpMessagingTemplate,
            FriendshipRequestService friendshipRequestService
    ) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.friendshipRequestService = friendshipRequestService;
    }

    @Data
    public static class GetFriendshipRequestsByUserIdDTO {
        private int userId;
    }

    @MessageMapping("/friendshipRequest.getFriendshipRequestsByUserId")
    public void getFriendshipRequestsByUserId(GetFriendshipRequestsByUserIdDTO getFriendshipRequestsByUserIdDTO) {
        List<FriendshipRequest> friendshipRequests = friendshipRequestService.getFriendshipRequestsByReceiverId(getFriendshipRequestsByUserIdDTO.getUserId());
        simpMessagingTemplate.convertAndSend("/topic/user/" + getFriendshipRequestsByUserIdDTO.getUserId() + "/friendshipRequests", friendshipRequests);
    }
}
