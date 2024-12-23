package org.adz1q.nextmessagebackend.repository;

import org.adz1q.nextmessagebackend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer> {
}
