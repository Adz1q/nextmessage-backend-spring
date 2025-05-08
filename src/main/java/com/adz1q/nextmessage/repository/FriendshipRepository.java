package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    @Query(value = "SELECT f.id FROM friendship f INNER JOIN friendship_member fm1 ON f.id = fm1.friendship_id AND fm1.user_id = :firstUserId INNER JOIN friendship_member fm2 ON f.id = fm2.friendship_id AND fm2.user_id = :secondUserId",nativeQuery = true)
    Optional<Integer> findFriendshipByUsers(@Param("firstUserId") int firstUserId, @Param("secondUserId") int secondUserId);
}
