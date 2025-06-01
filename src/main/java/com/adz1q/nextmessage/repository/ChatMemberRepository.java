package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Integer> {
    void deleteByUserId(int userId);
    void deleteByChatId(int chatId);
    List<ChatMember> findByUserId(int userId);
    List<ChatMember> findByChatId(int chatId);
    Optional<ChatMember> findByUserIdAndChatId(int userId, int chatId);

    @Modifying
    @Query(value="DELETE FROM chat_member WHERE chat_id = :chatId AND user_id = :userId", nativeQuery = true)
    void deleteByChatIdAndUserId(@Param("chatId") int chatId, @Param("userId") int userId);

    @Query(value = "SELECT cm.user_id FROM chat_member cm WHERE cm.chat_id = :chatId AND cm.user_id != :userId LIMIT 1", nativeQuery = true)
    Optional<Integer> findByChatIdAndNotUserId(@Param("chatId") int chatId, @Param("userId") int userId);
}
