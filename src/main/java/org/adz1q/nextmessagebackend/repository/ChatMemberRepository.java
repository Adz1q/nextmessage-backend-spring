package org.adz1q.nextmessagebackend.repository;

import org.adz1q.nextmessagebackend.model.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Integer> {
}
