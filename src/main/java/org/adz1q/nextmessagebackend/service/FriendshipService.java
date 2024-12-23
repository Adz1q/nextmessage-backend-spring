package org.adz1q.nextmessagebackend.service;

import org.adz1q.nextmessagebackend.model.Friendship;
import org.adz1q.nextmessagebackend.model.FriendshipMember;
import org.adz1q.nextmessagebackend.model.User;
import org.adz1q.nextmessagebackend.repository.FriendshipMemberRepository;
import org.adz1q.nextmessagebackend.repository.FriendshipRepository;
import org.adz1q.nextmessagebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public ResponseEntity<Object> addFriend(int senderId, int receiverId) {
        Friendship friendship = new Friendship();
        int friendshipId = friendship.getId();
        friendshipRepository.save(friendship);

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

    public ResponseEntity<Object> removeFriend(int friendshipId) {
        friendshipMemberRepository.deleteByFriendshipId(friendshipId);
        friendshipRepository.deleteById(friendshipId);

        return ResponseEntity.ok().body("Friend removed!");
    }

    public ResponseEntity<Object> getFriends(int userId) {
        List<FriendshipMember> friendshipMembers = friendshipMemberRepository.findByUserId(userId);
        List<Integer> friendshipIds = new ArrayList<>();

        for (FriendshipMember friendshipMember : friendshipMembers) {
            friendshipIds.add(friendshipMember.getFriendshipId());
        }

        List<User> friends = new ArrayList<>();

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

                    friends.add(optionalFriend.get());
                }
            }
        }

        return ResponseEntity.ok(friends);
    }
}