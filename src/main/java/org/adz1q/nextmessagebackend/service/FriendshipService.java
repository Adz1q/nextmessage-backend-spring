package org.adz1q.nextmessagebackend.service;

import lombok.Data;
import org.adz1q.nextmessagebackend.model.Friendship;
import org.adz1q.nextmessagebackend.model.FriendshipMember;
import org.adz1q.nextmessagebackend.model.User;
import org.adz1q.nextmessagebackend.repository.FriendshipMemberRepository;
import org.adz1q.nextmessagebackend.repository.FriendshipRepository;
import org.adz1q.nextmessagebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipMemberRepository friendshipMemberRepository;

    @Autowired
    public FriendshipService(
            UserRepository userRepository,
            FriendshipRepository friendshipRepository,
            FriendshipMemberRepository friendshipMemberRepository
    ) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendshipMemberRepository = friendshipMemberRepository;
    }

    @Data
    public static class Friend {
        private int id;
        private String username;
        private String profilePictureUrl;
        private int friendshipId;
        private LocalDateTime date;
    }

    public ResponseEntity<Object> addFriend(int senderId, int receiverId) {
        Friendship friendship = new Friendship();
        friendship.setDate(LocalDateTime.now());
        friendshipRepository.save(friendship);
        int friendshipId = friendship.getId();

        FriendshipMember friendshipMemberOne = new FriendshipMember();
        friendshipMemberOne.setFriendshipId(friendshipId);
        friendshipMemberOne.setUserId(senderId);
        friendshipMemberRepository.save(friendshipMemberOne);

        FriendshipMember friendshipMemberTwo = new FriendshipMember();
        friendshipMemberTwo.setFriendshipId(friendshipId);
        friendshipMemberTwo.setUserId(receiverId);
        friendshipMemberRepository.save(friendshipMemberTwo);

        return ResponseEntity.ok().body("Friend added!");
    }

    @Transactional
    public ResponseEntity<Object> removeFriend(int friendshipId) {
        Optional<Friendship> optionalFriendship = friendshipRepository.findById(friendshipId);

        if (optionalFriendship.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friendship not found");
        }

        friendshipMemberRepository.deleteByFriendshipId(friendshipId);
        friendshipRepository.deleteById(friendshipId);

        return ResponseEntity.ok().body("Friend removed!");
    }

    public List<Friend> getFriends(int userId) {
        List<FriendshipMember> friendshipMembers = friendshipMemberRepository.findByUserId(userId);
        List<Integer> friendshipIds = new ArrayList<>();

        for (FriendshipMember friendshipMember : friendshipMembers) {
            friendshipIds.add(friendshipMember.getFriendshipId());
        }

        List<Friend> friends = new ArrayList<>();

        for (int friendshipId : friendshipIds) {
            List<FriendshipMember> friendshipMembersByFriendshipId = friendshipMemberRepository.findByFriendshipId(friendshipId);

            for (FriendshipMember friendshipMember : friendshipMembersByFriendshipId) {
                if (friendshipMember.getUserId() != userId) {
                    Optional<User> optionalFriend = userRepository.findById(friendshipMember.getUserId());

                    if (optionalFriend.isEmpty()) {
                        friendshipMemberRepository.deleteByFriendshipId(friendshipId);
                        friendshipRepository.deleteById(friendshipId);
                        continue;
                    }

                    User user = optionalFriend.get();
                    Friend friend = new Friend();

                    Optional<Friendship> optionalFriendship = friendshipRepository.findById(friendshipId);

                    if (optionalFriendship.isEmpty()) {
                        friendshipMemberRepository.deleteByFriendshipId(friendshipId);
                        friendshipRepository.deleteById(friendshipId);
                        continue;
                    }

                    Friendship friendship = optionalFriendship.get();

                    friend.setId(user.getId());
                    friend.setUsername(user.getUsername());
                    friend.setProfilePictureUrl(user.getProfilePictureUrl());
                    friend.setFriendshipId(friendshipId);
                    friend.setDate(friendship.getDate());

                    friends.add(friend);
                }
            }
        }

        return friends;
    }
}