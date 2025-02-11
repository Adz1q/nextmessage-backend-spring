package com.adz1q.nextmessage.repository;

import com.adz1q.nextmessage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query(value = "SELECT * FROM \"user\" WHERE similarity(username, :username) >= 0.1 ORDER BY similarity(username, :username) DESC", nativeQuery = true)
    List<User> findBySimilarUsername(@Param("username") String username);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}