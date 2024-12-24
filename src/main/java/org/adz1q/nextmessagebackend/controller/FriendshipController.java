package org.adz1q.nextmessagebackend.controller;

import org.adz1q.nextmessagebackend.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/db/friendship")
public class FriendshipController {
    private final FriendshipService friendshipService;

    @Autowired
    public FriendshipController(FriendshipService friendshipService) {
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