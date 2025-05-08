package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    @Query(value = "SELECT pc.id FROM private_chat pc INNER JOIN chat_member cm1 ON pc.id = cm1.chat_id AND cm1.user_id = :firstUserId INNER JOIN chat_member cm2 ON pc.id = cm2.chat_id AND cm2.user_id = :secondUserId", nativeQuery = true)
    Optional<Integer> findPrivateChatByChatMembers(@Param("firstUserId") int firstUserId, @Param("secondUserId") int secondUserId);
}
