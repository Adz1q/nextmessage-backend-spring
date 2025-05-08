package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.model.FriendshipRequest;
import com.adz1q.nextmessage.service.FriendshipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friendshipRequest")
public class FriendshipRequestRestController {
    private final FriendshipRequestService friendshipRequestService;

    @Autowired
    public FriendshipRequestRestController(FriendshipRequestService friendshipRequestService) {
        this.friendshipRequestService = friendshipRequestService;
    }

    @PostMapping("/send")
    public ResponseEntity<Object> sendFriendshipRequest(@RequestBody FriendshipRequestService.SendFriendshipRequestDto sendFriendshipRequestDto) {
        return friendshipRequestService.sendFriendshipRequest(sendFriendshipRequestDto);
    }

    @PostMapping("/accept")
    public ResponseEntity<Object> acceptFriendshipRequest(@RequestBody FriendshipRequestService.AcceptFriendshipRequestDto acceptFriendshipRequestDto) {
        return friendshipRequestService.acceptFriendshipRequest(acceptFriendshipRequestDto);
    }

    @DeleteMapping("/reject")
    public ResponseEntity<Object> rejectFriendshipRequest(@RequestBody FriendshipRequestService.RejectFriendshipRequestDto rejectFriendshipRequestDto) {
        return friendshipRequestService.rejectFriendshipRequest(rejectFriendshipRequestDto);
    }

    @GetMapping("/getAll/{receiverId}")
    public List<FriendshipRequest> getFriendshipRequests(@PathVariable int receiverId) {
        return friendshipRequestService.getFriendshipRequests(receiverId);
    }

    @GetMapping("/getAllBySenderId/{senderId}")
    public List<FriendshipRequest> getFriendshipRequestsBySenderId(@PathVariable int senderId) {
        return friendshipRequestService.getFriendshipRequestsBySenderId(senderId);
    }
}