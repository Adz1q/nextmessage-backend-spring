package com.adz1q.nextmessage.service;

import lombok.Data;
import com.adz1q.nextmessage.model.FriendshipRequest;
import com.adz1q.nextmessage.model.User;
import com.adz1q.nextmessage.repository.FriendshipRequestRepository;
import com.adz1q.nextmessage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipRequestService {
    private final FriendshipService friendshipService;
    private final UserRepository userRepository;
    private final FriendshipRequestRepository friendshipRequestRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public FriendshipRequestService(
            FriendshipService friendshipService,
            UserRepository userRepository,
            FriendshipRequestRepository friendshipRequestRepository,
            SimpMessagingTemplate simpMessagingTemplate) {
        this.friendshipService = friendshipService;
        this.userRepository = userRepository;
        this.friendshipRequestRepository = friendshipRequestRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
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
        Optional<User> optionalSender = userRepository.findById(sendFriendshipRequestDto.getSenderId());
        Optional<User> optionalReceiver = userRepository.findById(sendFriendshipRequestDto.getReceiverId());

        if (optionalSender.isEmpty() || optionalReceiver.isEmpty()) {
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

        refreshUserFriendshipRequestsList(sendFriendshipRequestDto.getReceiverId());
//        refreshUserFriendshipRequestsList(sendFriendshipRequestDto.getSenderId()); //sending doesn't change the sender friendship requests list

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

        refreshUserFriendshipRequestsList(rejectFriendshipRequestDto.getReceiverId());
//        refreshUserFriendshipRequestsList(rejectFriendshipRequestDto.getSenderId()); //rejecting doesn't change the sender friendship requests list

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

        refreshUserFriendshipRequestsList(acceptFriendshipRequestDto.getReceiverId());
//        refreshUserFriendshipRequestsList(acceptFriendshipRequestDto.getSenderId()); //accepting doesn't change the sender friendship requests list

        return friendshipService.addFriend(
                acceptFriendshipRequestDto.getSenderId(),
                acceptFriendshipRequestDto.getReceiverId()
        );
    }

    public List<FriendshipRequest> getFriendshipRequestsByReceiverId(int receiverId) {
        return friendshipRequestRepository.findByReceiverId(receiverId);
    }

    public List<FriendshipRequest> getFriendshipRequestsBySenderId(int senderId) {
        return friendshipRequestRepository.findBySenderId(senderId);
    }

    public void refreshUserFriendshipRequestsList(int userId) {
        List<FriendshipRequest> friendshipRequests = getFriendshipRequestsByReceiverId(userId);
        simpMessagingTemplate.convertAndSend("/topic/user/" + userId + "/friendshipRequests", friendshipRequests);
    }
}