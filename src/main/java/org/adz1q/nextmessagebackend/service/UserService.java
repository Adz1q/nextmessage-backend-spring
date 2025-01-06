package org.adz1q.nextmessagebackend.service;

import lombok.Data;

import org.adz1q.nextmessagebackend.model.FriendshipMember;
import org.adz1q.nextmessagebackend.model.FriendshipRequest;
import org.adz1q.nextmessagebackend.model.User;
import org.adz1q.nextmessagebackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ChatMemberRepository chatMemberRepository;
    private final FriendshipMemberRepository friendshipMemberRepository;
    private final FriendshipRequestRepository friendshipRequestRepository;
    private final FriendshipRepository friendshipRepository;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ChatMemberRepository chatMemberRepository,
            FriendshipMemberRepository friendshipMemberRepository,
            FriendshipRequestRepository friendshipRequestRepository,
            FriendshipRepository friendshipRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.chatMemberRepository = chatMemberRepository;
        this.friendshipMemberRepository = friendshipMemberRepository;
        this.friendshipRequestRepository = friendshipRequestRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Data
    public static class JwtResponse {
        private String token;

        public JwtResponse(String token) {
            this.token = token;
        }
    }

    @Data
    public static class LoginRequest {
        private String login;
        private String password;
    }

    @Data
    public static class ChangeUsernameRequest {
        private int userId;
        private String newUsername;
    }

    @Data
    public static class ChangePasswordRequest {
        private int userId;
        private String oldPassword;
        private String newPassword;
    }

    @Data
    public static class ChangeMessagePreferencesRequest {
        private int userId;
        private boolean allowMessagesFromNonFriends;
    }

    @Data
    public static class DeleteAccountRequest {
        private int userId;
        private String password;
    }

    @Data
    public static class FoundUser {
        private int id;
        private String username;
        private String profilePictureUrl;
        private LocalDateTime date;
        private boolean allowMessagesFromNonFriends;
    }

    public ResponseEntity<Object> register(User user) {
        Optional<User> optionalUsername = userRepository.findByUsername(user.getUsername());
        Optional<User> optionalEmail = userRepository.findByEmail(user.getEmail());

        if(!optionalUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this username already exists");
        }

        if(!optionalEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this email already exists");
        }

        if(user.getUsername().length() < 4 || user.getUsername().length() > 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username must be between 4 and 20 characters");
        }

        if(user.getEmail().length() < 4 || user.getEmail().length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email must be between 4 and 50 characters");
        }

        if(user.getPassword().length() < 5 || user.getPassword().length() > 32) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be between 5 and 32 characters");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setProfilePictureUrl("default_profile_picture_url_user");
        user.setDate(LocalDateTime.now());
        user.setAllowMessagesFromNonFriends(true);

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<Object> login(LoginRequest loginRequest) {
        Optional<User> optionalUsername = userRepository.findByUsername(loginRequest.getLogin());
        Optional<User> optionalEmail = userRepository.findByEmail(loginRequest.getLogin());

        if(optionalUsername.isEmpty() && optionalEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid login or password");
        }

        User user = null;

        if(!optionalUsername.isEmpty()) {
            user = optionalUsername.get();
        }

        if(!optionalEmail.isEmpty()) {
            user = optionalEmail.get();
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login or password");
        }

        String token = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(new JwtResponse(token));
    }

    public ResponseEntity<Object> getUser(String login) {
        Optional<User> optionalUsername = userRepository.findByUsername(login);
        Optional<User> optionalEmail = userRepository.findByEmail(login);

        if(optionalUsername.isEmpty() && optionalEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
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

    public ResponseEntity<Object> changeUsername(ChangeUsernameRequest changeUsernameRequest) {
        Optional<User> optionalUser = userRepository.findById(changeUsernameRequest.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Optional<User> optionalUsername = userRepository.findByUsername(changeUsernameRequest.getNewUsername());

        if (!optionalUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is already taken");
        }

        if (changeUsernameRequest.getNewUsername().length() < 4 || changeUsernameRequest.getNewUsername().length() > 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New username must be between 4 and 20 characters");
        }

        User user = optionalUser.get();

        user.setUsername(changeUsernameRequest.getNewUsername());
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    public ResponseEntity<Object> changePassword(ChangePasswordRequest changePasswordRequest) {
        Optional<User> optionalUser = userRepository.findById(changePasswordRequest.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (changePasswordRequest.getNewPassword().length() < 5 || changePasswordRequest.getNewPassword().length() > 32) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New password must be between 5 and 32 characters");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        String newPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());

        user.setPassword(newPassword);
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    // changeProfilePicture()
    // deleteProfilePicture()

    public ResponseEntity<Object> changeAllowMessagesFromNonFriends(ChangeMessagePreferencesRequest changeMessagePreferencesRequest) {
        Optional<User> optionalUser = userRepository.findById(changeMessagePreferencesRequest.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        if (user.isAllowMessagesFromNonFriends() == changeMessagePreferencesRequest.isAllowMessagesFromNonFriends()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message preferences are already set to this value");
        }

        user.setAllowMessagesFromNonFriends(changeMessagePreferencesRequest.isAllowMessagesFromNonFriends());
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @Transactional
    public ResponseEntity<Object> deleteAccount(DeleteAccountRequest deleteAccountRequest) {
        Optional<User> optionalUser = userRepository.findById(deleteAccountRequest.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(deleteAccountRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        List<Integer> friendshipIds = new ArrayList<>();
        List<FriendshipMember> friendshipMembers = friendshipMemberRepository.findByUserId(deleteAccountRequest.getUserId());

        for (FriendshipMember friendshipMember: friendshipMembers) {
            friendshipIds.add(friendshipMember.getFriendshipId());
        }

        for (int friendshipId : friendshipIds) {
            friendshipMemberRepository.deleteByFriendshipId(friendshipId);
            friendshipRepository.deleteById(friendshipId);
        }

        List<Integer> friendshipRequestIds = new ArrayList<>();
        List<FriendshipRequest> friendshipRequestsByReceiverId = friendshipRequestRepository.findByReceiverId(deleteAccountRequest.getUserId());
        List<FriendshipRequest> friendshipRequestsBySenderId = friendshipRequestRepository.findBySenderId(deleteAccountRequest.getUserId());

        for (FriendshipRequest friendshipRequest : friendshipRequestsByReceiverId) {
            friendshipRequestIds.add(friendshipRequest.getId());
        }

        for (FriendshipRequest friendshipRequest : friendshipRequestsBySenderId) {
            friendshipRequestIds.add(friendshipRequest.getId());
        }

        for (int friendshipRequestId : friendshipRequestIds) {
            friendshipRequestRepository.deleteById(friendshipRequestId);
        }

        chatMemberRepository.deleteByUserId(deleteAccountRequest.getUserId());
        userRepository.deleteById(deleteAccountRequest.getUserId());

        return ResponseEntity.ok("Account deleted");
    }

    public List<FoundUser> getUsersBySimilarUsername(String username) {
        List<User> users = userRepository.findBySimilarUsername(username);
        List<FoundUser> foundUsers = new ArrayList<>();

        for (User user : users) {
            FoundUser foundUser = new FoundUser();

            foundUser.setId(user.getId());
            foundUser.setUsername(user.getUsername());
            foundUser.setProfilePictureUrl(user.getProfilePictureUrl());
            foundUser.setDate(user.getDate());
            foundUser.setAllowMessagesFromNonFriends(user.isAllowMessagesFromNonFriends());

            foundUsers.add(foundUser);
        }

        return foundUsers;
    }
}