package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.service.FriendshipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
    };
}
