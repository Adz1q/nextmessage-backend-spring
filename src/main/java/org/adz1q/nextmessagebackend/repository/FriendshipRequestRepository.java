package org.adz1q.nextmessagebackend.repository;

import org.adz1q.nextmessagebackend.model.FriendshipRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Integer> {
    Optional<FriendshipRequest> findBySenderIdAndReceiverId(int senderId, int receiverId);
}
