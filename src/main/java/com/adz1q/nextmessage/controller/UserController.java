package com.adz1q.nextmessage.controller;

import com.adz1q.nextmessage.model.User;
import com.adz1q.nextmessage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
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

    @GetMapping("/get/{login}")
    public ResponseEntity<Object> getUser(@PathVariable String login) {
        return userService.getUser(login);
    }

    @PostMapping("/change/username")
    public ResponseEntity<Object> changeUsername(@RequestBody UserService.ChangeUsernameRequest changeUsernameRequest) {
        return userService.changeUsername(changeUsernameRequest);
    }

    @PostMapping("/change/password")
    public ResponseEntity<Object> changePassword(@RequestBody UserService.ChangePasswordRequest changePasswordRequest) {
        return userService.changePassword(changePasswordRequest);
    }

    @PostMapping("change/messagePreferences")
    public ResponseEntity<Object> changeAllowMessagesFromNonFriends(@RequestBody UserService.ChangeMessagePreferencesRequest changeMessagePreferencesRequest) {
        return userService.changeAllowMessagesFromNonFriends(changeMessagePreferencesRequest);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteAccount(@RequestBody UserService.DeleteAccountRequest deleteAccountRequest) {
        return userService.deleteAccount(deleteAccountRequest);
    }

    @GetMapping("search/{username}")
    public List<UserService.FoundUser> getUsersBySimilarUsername(@PathVariable String username) {
        return userService.getUsersBySimilarUsername(username);
    }
}