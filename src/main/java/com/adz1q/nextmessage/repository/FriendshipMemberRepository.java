package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.FriendshipMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendshipMemberRepository extends JpaRepository<FriendshipMember, Integer> {
    void deleteByFriendshipId(int friendshipId);
    List<FriendshipMember> findByUserId(int userId);
    List<FriendshipMember> findByFriendshipId(int friendshipId);
}
