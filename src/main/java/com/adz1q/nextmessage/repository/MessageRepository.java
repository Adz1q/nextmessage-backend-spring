package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    @Query(value = "SELECT * FROM Message WHERE chat_id = :chatId ORDER BY date DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Message> findByChatId(@Param("chatId") int chatId, @Param("offset") int offset, @Param("limit") int limit);
    void deleteByChatId(int chatId);

    List<Message> findBySenderId(int senderId);

    @Query(value="DELETE FROM message WHERE chat_id = :chatId AND senderId = :senderId", nativeQuery = true)
    void deleteByChatIdAndSenderId(@Param("chatId") int chatId, @Param("senderId") int senderId);
}
