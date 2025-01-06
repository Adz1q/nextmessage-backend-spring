package org.adz1q.nextmessagebackend.repository;

import org.adz1q.nextmessagebackend.model.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Integer> {
    void deleteByUserId(int userId);
    void deleteByChatId(int chatId);
    List<ChatMember> findByUserId(int userId);
    List<ChatMember> findByChatId(int chatId);
    Optional<ChatMember> findByUserIdAndChatId(int userId, int chatId);
}
