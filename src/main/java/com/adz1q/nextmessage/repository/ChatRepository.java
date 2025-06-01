package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.Chat;
import com.adz1q.nextmessage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    @Query(value = "SELECT pc.id FROM private_chat pc INNER JOIN chat_member cm1 ON pc.id = cm1.chat_id AND cm1.user_id = :firstUserId INNER JOIN chat_member cm2 ON pc.id = cm2.chat_id AND cm2.user_id = :secondUserId", nativeQuery = true)
    Optional<Integer> findPrivateChatByChatMembers(@Param("firstUserId") int firstUserId, @Param("secondUserId") int secondUserId);

    @Query(value = "SELECT u.id, password, username, email, allow_messages_from_non_friends, date, profile_picture_url FROM private_chat pc INNER JOIN chat_member cm ON pc.id = cm.chat_id AND pc.id = :chatId INNER JOIN \"user\" u ON cm.user_id = u.id WHERE cm.user_id != :userId", nativeQuery = true)
    Optional<User> findByPrivateChatIdAndOtherUserId(@Param("chatId") int chatId, @Param("userId") int userId);

    @Query(value = "SELECT pc.id FROM private_chat pc INNER JOIN chat_member cm ON pc.id = cm.chat_id AND cm.user_id = :userId", nativeQuery = true)
    List<Integer> findPrivateChatsByChatMember(@Param("userId") int firstUserId);
}
