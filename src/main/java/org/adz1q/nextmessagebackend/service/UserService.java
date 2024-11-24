package org.adz1q.nextmessagebackend.service;

import org.adz1q.nextmessagebackend.model.User;
import org.adz1q.nextmessagebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<Object> register(User user) {
        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());

        if(!optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists!");
        }

        if(user.getUsername().length() < 4 || user.getUsername().length() > 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username must be between 4 and 20 characters!");
        }

        if(user.getPassword().length() < 5 || user.getPassword().length() > 32) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be between 5 and 32 characters!");
        }

        //user.setPassword();

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
