package org.adz1q.nextmessagebackend.repository;

import org.adz1q.nextmessagebackend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    @Query(value = "SELECT * FROM Message WHERE chat_id = :chatId ORDER BY date DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Message> findByChatId(@Param("chatId") int chatId, @Param("offset") int offset, @Param("limit") int limit);
}