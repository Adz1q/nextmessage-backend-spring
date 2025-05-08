package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
}
