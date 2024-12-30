package org.adz1q.nextmessagebackend.repository;

import org.adz1q.nextmessagebackend.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
}
