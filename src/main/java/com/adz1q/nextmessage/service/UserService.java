package com.adz1q.nextmessage.service;

import com.adz1q.nextmessage.repository.*;
import lombok.Data;

import com.adz1q.nextmessage.model.FriendshipMember;
import com.adz1q.nextmessage.model.FriendshipRequest;
import com.adz1q.nextmessage.model.User;
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
    public static class RegisterRequestDto {
        private String username;
        private String email;
        private String password;
    }

    @Data
    public static class LoginRequestDto {
        private String login;
        private String password;
    }

    @Data
    public static class ChangeUsernameRequestDto {
        private int userId;
        private String newUsername;
    }

    @Data
    public static class ChangePasswordRequestDto {
        private int userId;
        private String oldPassword;
        private String newPassword;
    }

    @Data
    public static class ChangeMessagePreferencesRequestDto {
        private int userId;
        private boolean allowMessagesFromNonFriends;
    }

    @Data
    public static class DeleteAccountRequestDto {
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

    public ResponseEntity<Object> register(RegisterRequestDto registerRequest) {
        String username = registerRequest.getUsername().trim();
        String email = registerRequest.getEmail().trim();
        String password = registerRequest.getPassword().trim();

        Optional<User> optionalUsername = userRepository.findByUsername(username);
        Optional<User> optionalEmail = userRepository.findByEmail(email);

        if (!optionalUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this username already exists");
        }

        if (!optionalEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this email already exists");
        }

        if (username.length() < 4 || username.length() > 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username must be between 4 and 20 characters");
        }

        if (email.length() < 4 || email.length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email must be between 4 and 50 characters");
        }

        if (password.length() < 5 || password.length() > 32) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be between 5 and 32 characters");
        }

        if (!username.matches("^[a-zA-Z].*")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username must start with a letter");
        }

        User user = new User();
        String encodedPassword = passwordEncoder.encode(password);
        String profilePictureUrl;
        LocalDateTime currentTime = LocalDateTime.now();
        boolean defaultAllowMessagesFromNonFriends = true;

        String firstLetter = username.substring(0, 1).toUpperCase();

        profilePictureUrl = switch (firstLetter) {
            case "A" -> "/profile-pictures/profile_picture_a.png";
            case "B" -> "/profile-pictures/profile_picture_b.png";
            case "C" -> "/profile-pictures/profile_picture_c.png";
            case "D" -> "/profile-pictures/profile_picture_d.png";
            case "E" -> "/profile-pictures/profile_picture_e.png";
            case "F" -> "/profile-pictures/profile_picture_f.png";
            case "G" -> "/profile-pictures/profile_picture_g.png";
            case "H" -> "/profile-pictures/profile_picture_h.png";
            case "I" -> "/profile-pictures/profile_picture_i.png";
            case "J" -> "/profile-pictures/profile_picture_j.png";
            case "K" -> "/profile-pictures/profile_picture_k.png";
            case "L" -> "/profile-pictures/profile_picture_l.png";
            case "M" -> "/profile-pictures/profile_picture_m.png";
            case "N" -> "/profile-pictures/profile_picture_n.png";
            case "O" -> "/profile-pictures/profile_picture_o.png";
            case "P" -> "/profile-pictures/profile_picture_p.png";
            case "Q" -> "/profile-pictures/profile_picture_q.png";
            case "R" -> "/profile-pictures/profile_picture_r.png";
            case "S" -> "/profile-pictures/profile_picture_s.png";
            case "T" -> "/profile-pictures/profile_picture_t.png";
            case "U" -> "/profile-pictures/profile_picture_u.png";
            case "V" -> "/profile-pictures/profile_picture_v.png";
            case "W" -> "/profile-pictures/profile_picture_w.png";
            case "X" -> "/profile-pictures/profile_picture_x.png";
            case "Y" -> "/profile-pictures/profile_picture_y.png";
            case "Z" -> "/profile-pictures/profile_picture_z.png";
            default -> "/profile-pictures/profile_picture_default.png";
        };

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setProfilePictureUrl(profilePictureUrl);
        user.setDate(currentTime);
        user.setAllowMessagesFromNonFriends(defaultAllowMessagesFromNonFriends);

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<Object> login(LoginRequestDto loginRequest) {
        String login = loginRequest.getLogin().trim();
        String password = loginRequest.getPassword().trim();

        if (password.contains(" ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid login or password");
        }

        Optional<User> optionalUsername = userRepository.findByUsername(login);
        Optional<User> optionalEmail = userRepository.findByEmail(login);

        if (optionalUsername.isEmpty() && optionalEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid login or password");
        }

        User user = null;

        if (!optionalUsername.isEmpty()) {
            user = optionalUsername.get();
        }

        if (!optionalEmail.isEmpty()) {
            user = optionalEmail.get();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid login or password");
        }

        String token = jwtService.generateToken(user.getUsername());
        JwtResponse jwtResponse = new JwtResponse(token);

        return ResponseEntity.ok(jwtResponse);
    }

    public ResponseEntity<Object> getUser(String login) {
        Optional<User> optionalUsername = userRepository.findByUsername(login);
        Optional<User> optionalEmail = userRepository.findByEmail(login);

        if (optionalUsername.isEmpty() && optionalEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = null;

        if (!optionalUsername.isEmpty()) {
            user = optionalUsername.get();
        }

        if (!optionalEmail.isEmpty()) {
            user = optionalEmail.get();
        }

        return ResponseEntity.ok(user);
    }

    public ResponseEntity<Object> changeUsername(ChangeUsernameRequestDto changeUsernameRequest) {
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

    public ResponseEntity<Object> changePassword(ChangePasswordRequestDto changePasswordRequest) {
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

    public ResponseEntity<Object> changeAllowMessagesFromNonFriends(ChangeMessagePreferencesRequestDto changeMessagePreferencesRequest) {
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
    public ResponseEntity<Object> deleteAccount(DeleteAccountRequestDto deleteAccountRequest) {
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

        for (FriendshipMember friendshipMember : friendshipMembers) {
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