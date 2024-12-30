package org.adz1q.nextmessagebackend.repository;

import org.adz1q.nextmessagebackend.model.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Integer> {
    void deleteByUserId(int userId);
    List<ChatMember> findByUserId(int userId);
}
