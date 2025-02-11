package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
}
