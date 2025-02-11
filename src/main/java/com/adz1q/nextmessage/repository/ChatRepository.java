package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
}
