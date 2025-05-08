package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friendship")
public class FriendshipRestController {
    private final FriendshipService friendshipService;

    @Autowired
    public FriendshipRestController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @DeleteMapping("/remove/{friendshipId}")
    public ResponseEntity<Object> removeFriend(@PathVariable int friendshipId) {
        return friendshipService.removeFriend(friendshipId);
    }

    @GetMapping("/getAll/{userId}")
    public List<FriendshipService.Friend> getFriends(@PathVariable int userId) {
        return friendshipService.getFriends(userId);
    }
}