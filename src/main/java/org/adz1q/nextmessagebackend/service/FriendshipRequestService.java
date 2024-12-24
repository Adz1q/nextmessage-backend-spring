package org.adz1q.nextmessagebackend.service;

import lombok.Data;
import org.adz1q.nextmessagebackend.model.FriendshipRequest;
import org.adz1q.nextmessagebackend.model.User;
import org.adz1q.nextmessagebackend.repository.FriendshipRequestRepository;
import org.adz1q.nextmessagebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Data
    public static class SendFriendshipRequestDto {
        private int senderId;
        private int receiverId;
    }

    @Data
    public static class RejectFriendshipRequestDto {
        private int senderId;
        private int receiverId;
    }

    @Data
    public static class AcceptFriendshipRequestDto {
        private int senderId;
        private int receiverId;
    }

    public ResponseEntity<Object> sendFriendshipRequest(SendFriendshipRequestDto sendFriendshipRequestDto) {
        Optional<User> sender = userRepository.findById(sendFriendshipRequestDto.getSenderId());
        Optional<User> receiver = userRepository.findById(sendFriendshipRequestDto.getReceiverId());

        if (sender.isEmpty() || receiver.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Receiver or sender not found!");
        }

        if (sendFriendshipRequestDto.getSenderId() == sendFriendshipRequestDto.getReceiverId()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can't send a friendship request to yourself!");
        }

        Optional<FriendshipRequest> optionalFriendshipRequest = friendshipRequestRepository.findBySenderIdAndReceiverId(
                sendFriendshipRequestDto.getSenderId(),
                sendFriendshipRequestDto.getReceiverId()
        );

        if (!optionalFriendshipRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friendship request already sent!");
        }

        List<FriendshipService.Friend> receiverFriends = friendshipService.getFriends(sendFriendshipRequestDto.getReceiverId());

        for (FriendshipService.Friend receiverFriend : receiverFriends) {
            if (receiverFriend.getId() == sendFriendshipRequestDto.getSenderId()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are already friends!");
            }
        }

        FriendshipRequest friendshipRequest = new FriendshipRequest();
        friendshipRequest.setSenderId(sendFriendshipRequestDto.getSenderId());
        friendshipRequest.setReceiverId(sendFriendshipRequestDto.getReceiverId());
        friendshipRequest.setDate(LocalDateTime.now());
        friendshipRequestRepository.save(friendshipRequest);

        return ResponseEntity.ok().body("Friendship request sent!");
    }

    public ResponseEntity<Object> rejectFriendshipRequest(RejectFriendshipRequestDto rejectFriendshipRequestDto) {
        Optional<FriendshipRequest> optionalFriendshipRequest = friendshipRequestRepository.findBySenderIdAndReceiverId(
                rejectFriendshipRequestDto.getSenderId(),
                rejectFriendshipRequestDto.getReceiverId()
        );

        if (optionalFriendshipRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friendship request not found!");
        }

        FriendshipRequest friendshipRequest = optionalFriendshipRequest.get();
        friendshipRequestRepository.deleteById(friendshipRequest.getId());

        return ResponseEntity.ok().body("Friendship request rejected!");
    }

    public ResponseEntity<Object> acceptFriendshipRequest(AcceptFriendshipRequestDto acceptFriendshipRequestDto) {
        Optional<FriendshipRequest> optionalFriendshipRequest = friendshipRequestRepository.findBySenderIdAndReceiverId(
                acceptFriendshipRequestDto.getSenderId(),
                acceptFriendshipRequestDto.getReceiverId()
        );

        if (optionalFriendshipRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friendship request not found!");
        }

        FriendshipRequest friendshipRequest = optionalFriendshipRequest.get();
        friendshipRequestRepository.deleteById(friendshipRequest.getId());

        return friendshipService.addFriend(
                acceptFriendshipRequestDto.getSenderId(),
                acceptFriendshipRequestDto.getReceiverId()
        );
    }

    public List<FriendshipRequest> getFriendshipRequests(int receiverId) {
        return friendshipRequestRepository.findByReceiverId(receiverId);
    }
}