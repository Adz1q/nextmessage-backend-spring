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
        private String login;
        private String password;

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }

    public ResponseEntity<Object> register(User user) {
        Optional<User> optionalUsername = userRepository.findByUsername(user.getUsername());
        Optional<User> optionalEmail = userRepository.findByEmail(user.getEmail());

        if(!optionalUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists!");
        }

        if(!optionalEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists!");
        }

        if(user.getUsername().length() < 4 || user.getUsername().length() > 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username must be between 4 and 20 characters!");
        }

        if(user.getEmail().length() < 4 || user.getEmail().length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email must be between 8 and 50 characters");
        }

        if(user.getPassword().length() < 5 || user.getPassword().length() > 32) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be between 5 and 32 characters!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<Object> login(LoginRequest loginRequest) {
        Optional<User> optionalUsername = userRepository.findByUsername(loginRequest.getLogin());
        Optional<User> optionalEmail = userRepository.findByEmail(loginRequest.getLogin());

        if(optionalUsername.isEmpty() && optionalEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid username or password!");
        }

        User user = null;

        if(!optionalUsername.isEmpty()) {
            user = optionalUsername.get();
        }

        if(!optionalEmail.isEmpty()) {
            user = optionalEmail.get();
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password!");
        }

        String token = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(new JwtResponse(token));
    }

    public ResponseEntity<Object> getUser(String login) {
        Optional<User> optionalUsername = userRepository.findByUsername(login);
        Optional<User> optionalEmail = userRepository.findByEmail(login);

        if(optionalUsername.isEmpty() && optionalEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        User user = null;

        if(!optionalUsername.isEmpty()) {
            user = optionalUsername.get();
        }

        if(!optionalEmail.isEmpty()) {
            user = optionalEmail.get();
        }

        return ResponseEntity.ok(user);
    }
}