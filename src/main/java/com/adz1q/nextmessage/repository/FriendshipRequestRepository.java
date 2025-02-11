package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.FriendshipRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Integer> {
    Optional<FriendshipRequest> findBySenderIdAndReceiverId(int senderId, int receiverId);
    List<FriendshipRequest> findByReceiverId(int receiverId);
    List<FriendshipRequest> findBySenderId(int senderId);
}
