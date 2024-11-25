package org.adz1q.nextmessagebackend.controller;

import org.adz1q.nextmessagebackend.model.User;
import org.adz1q.nextmessagebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/db/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserService.LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }
}