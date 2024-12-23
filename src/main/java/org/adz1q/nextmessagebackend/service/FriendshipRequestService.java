package org.adz1q.nextmessagebackend.service;

import org.adz1q.nextmessagebackend.model.FriendshipRequest;
import org.adz1q.nextmessagebackend.model.User;
import org.adz1q.nextmessagebackend.repository.FriendshipRepository;
import org.adz1q.nextmessagebackend.repository.FriendshipRequestRepository;
import org.adz1q.nextmessagebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendshipRequestService {
    private final FriendshipService friendshipService;
    private final UserRepository userRepository;
    private final FriendshipRequestRepository friendshipRequestRepository;

    @Autowired
    public FriendshipRequestService(
            FriendshipService friendshipService,
            UserRepository userRepository,
            FriendshipRequestRepository friendshipRequestRepository
    ) {
        this.friendshipService = friendshipService;
        this.userRepository = userRepository;
        this.friendshipRequestRepository = friendshipRequestRepository;
    }

    public ResponseEntity<Object> addFriendshipRequest(int senderId, int receiverId) {
        Optional<User> sender = userRepository.findById(senderId);
        Optional<User> receiver = userRepository.findById(receiverId);

        if (sender.isEmpty() || receiver.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Receiver or sender not found!");
        }

        if (senderId == receiverId) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can't send a friendship request to yourself!");
        }

        Optional<FriendshipRequest> optionalFriendshipRequest = friendshipRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);

        if (!optionalFriendshipRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friendship request already sent!");
        }

        List<User> receiverFriends = (List<User>) friendshipService.getFriends(receiverId);

        for (User receiverFriend : receiverFriends) {
            if (receiverFriend.getId() == senderId) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are already friends!");
            }
        }

        FriendshipRequest friendshipRequest = new FriendshipRequest();
        friendshipRequest.setSenderId(senderId);
        friendshipRequest.setReceiverId(receiverId);
        friendshipRequestRepository.save(friendshipRequest);

        return ResponseEntity.ok().body("Friendship request sent!");
    }

    public ResponseEntity<Object> rejectFriendshipRequest(int senderId, int receiverId) {
        Optional<FriendshipRequest> optionalFriendshipRequest = friendshipRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);

        if (optionalFriendshipRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friendship request not found!");
        }

        FriendshipRequest friendshipRequest = optionalFriendshipRequest.get();
        friendshipRequestRepository.deleteById(friendshipRequest.getId());

        return ResponseEntity.ok().body("Friendship request rejected!");
    }

    public ResponseEntity<Object> acceptFriendshipRequest(int senderId, int receiverId) {
        Optional<FriendshipRequest> optionalFriendshipRequest = friendshipRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);

        if (optionalFriendshipRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friendship request not found!");
        }

        FriendshipRequest friendshipRequest = optionalFriendshipRequest.get();
        friendshipRequestRepository.deleteById(friendshipRequest.getId());

        return friendshipService.addFriend(senderId, receiverId);
    }
}