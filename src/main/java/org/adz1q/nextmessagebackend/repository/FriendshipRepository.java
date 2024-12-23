package org.adz1q.nextmessagebackend.repository;

import org.adz1q.nextmessagebackend.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
}
