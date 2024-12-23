package org.adz1q.nextmessagebackend.repository;

import org.adz1q.nextmessagebackend.model.PrivateChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateChatRepository extends JpaRepository<PrivateChat, Integer> {
}
