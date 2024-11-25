package org.adz1q.nextmessagebackend.service;

import org.adz1q.nextmessagebackend.model.User;
import org.adz1q.nextmessagebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public static class JwtResponse {
        private String token;

        public JwtResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
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

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<Object> login(LoginRequest loginRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());

        if(optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        User user = optionalUser.get();

        if(!user.getUsername().equals(loginRequest.getUsername()) || !user.getPassword().equals(passwordEncoder.encode(loginRequest.getPassword()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials!");
        }

        String token = jwtService.generateToken(loginRequest.getUsername());

        return ResponseEntity.ok(new JwtResponse(token));
    }
}