package com.adz1q.nextmessage.service;

import com.adz1q.nextmessage.model.*;
import com.adz1q.nextmessage.repository.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {
    private final MessageRepository messageRepository;
    private final SecretKey secretKey;
    private final PrivateChatRepository privateChatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final FriendshipMemberRepository friendshipMemberRepository;
    private final TeamChatRepository teamChatRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public ChatService(
            MessageRepository messageRepository,
            PrivateChatRepository privateChatRepository,
            ChatMemberRepository chatMemberRepository,
            UserRepository userRepository,
            ChatRepository chatRepository,
            FriendshipMemberRepository friendshipMemberRepository,
            TeamChatRepository teamChatRepository,
            SimpMessagingTemplate simpMessagingTemplate
    ) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);

        this.secretKey = keyGenerator.generateKey();
        this.messageRepository = messageRepository;
        this.privateChatRepository = privateChatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.friendshipMemberRepository = friendshipMemberRepository;
        this.teamChatRepository = teamChatRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Data
    public static class EncryptedMessage {
        private String content;
        private SecretKey secretKey;
    }

    @Data
    public static class ChatCard {
        private int id;
        private String name;
        private LocalDateTime lastUpdated;
        private String profilePictureUrl;
        private String type;
    }

    @Data
    public static class MessageDto {
        private int id;
        private int chatId;
        private int senderId;
        private String content;
        private LocalDateTime date;
    }

    @Data
    public static class PrivateChatRequestDto {
        private int senderId;
        private int receiverId;
    }

    @Data
    public static class OtherChatMemberDto {
        private int id;
        private String username;
        private String profilePictureUrl;
        private LocalDateTime date;
        private boolean isFriend;
    }

    @Data
    public static class TeamChatRequestDto {
        private String name;
        private String profilePictureUrl;
        private int adminId;
        List<Integer> memberIds;
    }

    @Data
    public static class ChangeTeamChatNameRequestDto {
        private int chatId;
        private int userId;
        private String name;
    }

    @Data
    public static class DeleteTeamChatProfilePictureRequestDto {
        private int chatId;
        private int userId;
    }

    @Data
    public static class ChangeTeamChatAdminRequestDto {
        private int chatId;
        private int userId;
        private int newAdminId;
    }

    @Data
    public static class AddTeamChatMemberRequestDto {
        private int chatId;
        private int userId;
        private int newMemberId;
    }

    @Data
    public static class RemoveTeamChatMemberRequestDto {
        private int chatId;
        private int userId;
        private int memberId;
    }

    @Data
    public static class DeleteTeamChatRequestDto {
        private int chatId;
        private int userId;
    }

    public EncryptedMessage encryptMessage(String content) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedContent = cipher.doFinal(content.getBytes());

        EncryptedMessage encryptedMessage = new EncryptedMessage();

        encryptedMessage.setContent(Base64.getEncoder().encodeToString(encryptedContent));
        encryptedMessage.setSecretKey(secretKey);

        return encryptedMessage;
    }

    public String decryptMessage(String encryptedContent, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedContent = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));

        return new String(decryptedContent);
    }

    public int saveMessageAndReturnId(ChatMessage chatMessage) {
        Message message = new Message();

        message.setChatId(chatMessage.getChatId());
        message.setSenderId(chatMessage.getSenderId());
        message.setContent(chatMessage.getContent());
        message.setDate(chatMessage.getDate());
        message.setSecretKey(chatMessage.getSecretKey());

        messageRepository.save(message);

        return message.getId();
    }

    public MessageDto sendMessage(ChatMessage chatMessage) throws Exception {
        Optional<Chat> optionalChat = chatRepository.findById(chatMessage.getChatId());
        Optional<User> optionalUser = userRepository.findById(chatMessage.getSenderId());

        if (optionalChat.isEmpty()) {
            throw new Exception("Chat not found");
        }

        if (optionalUser.isEmpty()) {
            throw new Exception("User not found");
        }

        Optional<ChatMember> optionalChatMember = chatMemberRepository.findByUserIdAndChatId(chatMessage.getSenderId(), chatMessage.getChatId());

        if (optionalChatMember.isEmpty()) {
            throw new Exception("User is not a member of this chat");
        }

        EncryptedMessage encryptedMessage = encryptMessage(chatMessage.getContent());

        chatMessage.setContent(encryptedMessage.getContent());
        chatMessage.setDate(LocalDateTime.now());
        chatMessage.setSecretKey(encryptedMessage.getSecretKey());

        int id = saveMessageAndReturnId(chatMessage);

        Chat chat = optionalChat.get();

        chat.setLastUpdated(LocalDateTime.now());
        chatRepository.save(chat);

        String decryptedContent = decryptMessage(chatMessage.getContent(), chatMessage.getSecretKey());

        MessageDto messageDto = new MessageDto();

        messageDto.setId(id);
        messageDto.setChatId(chatMessage.getChatId());
        messageDto.setSenderId(chatMessage.getSenderId());
        messageDto.setContent(decryptedContent);
        messageDto.setDate(chatMessage.getDate());

        List<ChatMember> chatMembers = chatMemberRepository.findByChatId(chat.getId());

        for (ChatMember chatMember : chatMembers) {
            refreshUserChatList(chatMember.getUserId());
        }

        return messageDto;
    }

    public void refreshUserChatList(int userId) {
        List<ChatCard> chatCards = getChatsByUserId(userId);
        simpMessagingTemplate.convertAndSend("/topic/user/" + userId + "/chats", chatCards);
    }

    public ResponseEntity<Object> getMessages(int chatId, int userId, int offset, int limit) {
        Optional<ChatMember> optionalChatMember = chatMemberRepository.findByUserIdAndChatId(userId, chatId);

        if (optionalChatMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of this chat");
        }

        List<Message> messages = messageRepository.findByChatId(chatId, offset, limit);
        List<MessageDto> messageDtos = new ArrayList<>();

        for (Message message : messages) {
            try {
                String decryptedContent = decryptMessage(message.getContent(), message.getSecretKey());
                message.setContent(decryptedContent);

                MessageDto messageDto = new MessageDto();
                messageDto.setId(message.getId());
                messageDto.setChatId(message.getChatId());
                messageDto.setSenderId(message.getSenderId());
                messageDto.setContent(message.getContent());
                messageDto.setDate(message.getDate());

                messageDtos.add(messageDto);
            }
            catch (Exception error) {
                System.out.println("Error while decrypting message");
            }
        }

        return ResponseEntity.ok(messageDtos);
    }

    public ResponseEntity<Object> getChat(int chatId, int userId) {
        Optional<ChatMember> optionalChatMember = chatMemberRepository.findByUserIdAndChatId(userId, chatId);

        if (optionalChatMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of this chat");
        }

        Optional<PrivateChat> optionalPrivateChat = privateChatRepository.findById(chatId);
        Optional<TeamChat> optionalTeamChat = teamChatRepository.findById(chatId);

        if (!optionalPrivateChat.isEmpty()) {
            return ResponseEntity.ok(optionalPrivateChat.get());
        }

        if (!optionalTeamChat.isEmpty()) {
            return ResponseEntity.ok(optionalPrivateChat.get());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
    }

    public List<ChatCard> getChatsByUserId(int userId) {
//        Optional<User> optionalUser = userRepository.findById(userId);
//
////        if (optionalUser.isEmpty()) {
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The user does not exist");
////        }

        List<ChatMember> chatMembers = chatMemberRepository.findByUserId(userId);
        List<ChatCard> chats = new ArrayList<>();

        for (ChatMember chatMember : chatMembers) {
            Optional<Chat> optionalChat = chatRepository.findById(chatMember.getChatId());

            if (optionalChat.isEmpty()) {
                continue;
            }

            Chat chat = optionalChat.get();
            ChatCard chatCard = new ChatCard();
            int id = chat.getId();
            String name = "";
            LocalDateTime lastUpdated = chat.getLastUpdated();
            String profilePictureUrl = "";
            String type = "";

            Optional<PrivateChat> optionalPrivateChat = privateChatRepository.findById(chat.getId());
            Optional<TeamChat> optionalTeamChat = teamChatRepository.findById(chat.getId());

            if (!optionalPrivateChat.isEmpty()) {
                List<ChatMember> privateChatMembers = chatMemberRepository.findByChatId(id);
                User otherUser = null;

                for (ChatMember privateChatMember : privateChatMembers) {
                    if (privateChatMember.getUserId() != userId) {
                        Optional<User> optionalOtherUser = userRepository.findById(privateChatMember.getUserId());

                        if (optionalOtherUser.isEmpty()) {
                            otherUser.setUsername("Deleted user");
                            otherUser.setProfilePictureUrl("/profile-pictures/profile_picture_default.png");
                            break;
                        }

                        otherUser = optionalOtherUser.get();
                        break;
                    }
                }

                profilePictureUrl = otherUser.getProfilePictureUrl();
                name = otherUser.getUsername();
                type = "private";
            }

            if (!optionalTeamChat.isEmpty()) {
                TeamChat teamChat = optionalTeamChat.get();

                profilePictureUrl = teamChat.getProfilePictureUrl();
                name = teamChat.getName();
                lastUpdated = teamChat.getLastUpdated();
                type = "team";
            }

            chatCard.setId(id);
            chatCard.setName(name);
            chatCard.setLastUpdated(lastUpdated);
            chatCard.setProfilePictureUrl(profilePictureUrl);
            chatCard.setType(type);

            chats.add(chatCard);
        }

//        return ResponseEntity.ok(chats);
        return chats;
    }

    public ResponseEntity<Object> getChatMembers(int chatId, int userId) {
        Optional<ChatMember> optionalChatMember = chatMemberRepository.findByUserIdAndChatId(userId, chatId);

        if (optionalChatMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of this chat");
        }

        Optional<Chat> optionalChat = chatRepository.findById(chatId);

        if (optionalChat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        List<ChatMember> chatMembers = chatMemberRepository.findByChatId(chatId);

        return ResponseEntity.ok(chatMembers);
    }

    public ResponseEntity<Object> getOtherPrivateChatMember(int chatId, int userId) {
        Optional<ChatMember> optionalChatMember = chatMemberRepository.findByUserIdAndChatId(userId, chatId);

        if (optionalChatMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of this chat");
        }

        Optional<Chat> optionalChat = chatRepository.findById(chatId);

        if (optionalChat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        List<ChatMember> chatMembers = chatMemberRepository.findByChatId(chatId);
        OtherChatMemberDto otherChatMemberDto = new OtherChatMemberDto();

        for (ChatMember chatMember : chatMembers) {
            if (chatMember.getUserId() != userId) {
                Optional<User> optionalUser = userRepository.findById(chatMember.getUserId());

                if (optionalUser.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
                }

                User user = optionalUser.get();

                otherChatMemberDto.setId(user.getId());
                otherChatMemberDto.setUsername(user.getUsername());
                otherChatMemberDto.setProfilePictureUrl(user.getProfilePictureUrl());
                otherChatMemberDto.setDate(user.getDate());
                otherChatMemberDto.setFriend(false);

                List<FriendshipMember> friendshipMembersUser = friendshipMemberRepository.findByUserId(userId);
                List<FriendshipMember> friendshipMembersOtherUser = friendshipMemberRepository.findByUserId(user.getId());

                for (FriendshipMember friendshipMemberUser : friendshipMembersUser) {
                    for (FriendshipMember friendshipMemberOtherUser : friendshipMembersOtherUser) {
                        if (friendshipMemberUser.getFriendshipId() == friendshipMemberOtherUser.getFriendshipId()) {
                            otherChatMemberDto.setFriend(true);
                            break;
                        }
                    }
                }

                break;
            }
        }

        return ResponseEntity.ok(otherChatMemberDto);
    }

    public ResponseEntity<Object> createPrivateChat(PrivateChatRequestDto privateChatRequestDto) {
        Optional<User> optionalSender = userRepository.findById(privateChatRequestDto.getSenderId());
        Optional<User> optionalReceiver = userRepository.findById(privateChatRequestDto.getReceiverId());

        if (optionalSender.isEmpty() || optionalReceiver.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        List<ChatMember> chatMembersSender = chatMemberRepository.findByUserId(privateChatRequestDto.getSenderId());
        List<ChatMember> chatMembersReceiver = chatMemberRepository.findByUserId(privateChatRequestDto.getReceiverId());

        List<Integer> senderChatIds = new ArrayList<>();
        List<Integer> receiverChatIds = new ArrayList<>();

        for (ChatMember chatMember : chatMembersSender) {
            senderChatIds.add(chatMember.getChatId());
        }

        for (ChatMember chatMember : chatMembersReceiver) {
            receiverChatIds.add(chatMember.getChatId());
        }

        for (int senderChatId : senderChatIds) {
            for (int receiverChatId : receiverChatIds) {
                if (senderChatId == receiverChatId) {
                    Optional<PrivateChat> optionalPrivateChat = privateChatRepository.findById(senderChatId);

                    if (!optionalPrivateChat.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat already exists");
                    }
                }
            }
        }

        PrivateChat privateChat = new PrivateChat();

        privateChat.setLastUpdated(LocalDateTime.now());
        privateChatRepository.save(privateChat);

        ChatMember chatMemberOne = new ChatMember();
        ChatMember chatMemberTwo = new ChatMember();

        chatMemberOne.setChatId(privateChat.getId());
        chatMemberOne.setUserId(privateChatRequestDto.getSenderId());

        chatMemberTwo.setChatId(privateChat.getId());
        chatMemberTwo.setUserId(privateChatRequestDto.getReceiverId());

        chatMemberRepository.save(chatMemberOne);
        chatMemberRepository.save(chatMemberTwo);

        return ResponseEntity.ok(privateChat);
    }

    public ResponseEntity<Object> createTeamChat(TeamChatRequestDto teamChatRequestDto) {
        String name = teamChatRequestDto.getName().trim();
        String profilePictureUrl = teamChatRequestDto.getProfilePictureUrl().trim();
        int adminId = teamChatRequestDto.getAdminId();
        List<Integer> memberIds = teamChatRequestDto.getMemberIds();
        LocalDateTime lastUpdated = LocalDateTime.now();

        Optional<User> optionalUser = userRepository.findById(adminId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        if (name.isEmpty() || name.length() > 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name must be between 1 and 20 characters");
        }

        TeamChat teamChat = new TeamChat();

        teamChat.setName(name);
        teamChat.setProfilePictureUrl(profilePictureUrl);
        teamChat.setAdminId(adminId);
        teamChat.setLastUpdated(lastUpdated);

        chatRepository.save(teamChat);

        ChatMember chatMember = new ChatMember();

        chatMember.setChatId(teamChat.getId());
        chatMember.setUserId(adminId);

        chatMemberRepository.save(chatMember);

        for (int memberId : memberIds) {
            Optional<User> optionalMember = userRepository.findById(memberId);

            if (optionalMember.isEmpty()) {
                continue;
            }

            ChatMember newChatMember = new ChatMember();
            newChatMember.setChatId(teamChat.getId());
            newChatMember.setUserId(memberId);

            chatMemberRepository.save(newChatMember);
        }

        return ResponseEntity.ok(teamChat);
    }

    public ResponseEntity<Object> changeTeamChatName(ChangeTeamChatNameRequestDto changeTeamChatRequestDto) {
        Optional<User> optionalUser = userRepository.findById(changeTeamChatRequestDto.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        Optional<TeamChat> optionalTeamChat = teamChatRepository.findById(changeTeamChatRequestDto.getChatId());

        if (optionalTeamChat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        TeamChat teamChat = optionalTeamChat.get();

        if (teamChat.getAdminId() != changeTeamChatRequestDto.getUserId()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not the admin of this chat");
        }

        if (changeTeamChatRequestDto.getName().isEmpty() || changeTeamChatRequestDto.getName().length() > 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name must be between 1 and 20 characters");
        }

        teamChat.setName(changeTeamChatRequestDto.getName());
        teamChat.setLastUpdated(LocalDateTime.now());

        teamChatRepository.save(teamChat);

        return ResponseEntity.ok(teamChat);
    }

    //changeTeamChatProfilePicture()

    public ResponseEntity<Object> deleteTeamChatProfilePicture(DeleteTeamChatProfilePictureRequestDto deleteTeamChatProfilePictureRequestDto) {
        Optional<User> optionalUser = userRepository.findById(deleteTeamChatProfilePictureRequestDto.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        Optional<TeamChat> optionalTeamChat = teamChatRepository.findById(deleteTeamChatProfilePictureRequestDto.getChatId());

        if (optionalTeamChat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        TeamChat teamChat = optionalTeamChat.get();

        if (teamChat.getAdminId() != deleteTeamChatProfilePictureRequestDto.getUserId()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not the admin of this chat");
        }

        teamChat.setProfilePictureUrl("/profile.png");
        teamChat.setLastUpdated(LocalDateTime.now());

        teamChatRepository.save(teamChat);

        return ResponseEntity.ok(teamChat);
    }

    public ResponseEntity<Object> changeTeamChatAdmin(ChangeTeamChatAdminRequestDto changeTeamChatAdminRequestDto) {
        Optional<User> optionalUser = userRepository.findById(changeTeamChatAdminRequestDto.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        Optional<User> optionalNewAdmin = userRepository.findById(changeTeamChatAdminRequestDto.getNewAdminId());

        if (optionalNewAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New admin not found");
        }

        Optional<TeamChat> optionalTeamChat = teamChatRepository.findById(changeTeamChatAdminRequestDto.getChatId());

        if (optionalTeamChat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        Optional<ChatMember> optionalChatMemberUser = chatMemberRepository.findByUserIdAndChatId(changeTeamChatAdminRequestDto.getUserId(), changeTeamChatAdminRequestDto.getChatId());

        if (optionalChatMemberUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of this chat");
        }

        Optional<ChatMember> optionalChatMemberNewAdmin = chatMemberRepository.findByUserIdAndChatId(changeTeamChatAdminRequestDto.getNewAdminId(), changeTeamChatAdminRequestDto.getChatId());

        if (optionalChatMemberNewAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New admin is not a member of this chat");
        }

        TeamChat teamChat = optionalTeamChat.get();

        if (teamChat.getAdminId() != changeTeamChatAdminRequestDto.getUserId()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not the admin of this chat");
        }

        if (teamChat.getAdminId() == changeTeamChatAdminRequestDto.getNewAdminId()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is already the admin of this chat");
        }

        teamChat.setAdminId(changeTeamChatAdminRequestDto.getNewAdminId());
        teamChat.setLastUpdated(LocalDateTime.now());

        teamChatRepository.save(teamChat);

        return ResponseEntity.ok(teamChat);
    }

    public ResponseEntity<Object> addTeamChatMember(AddTeamChatMemberRequestDto addTeamChatMemberRequestDto) {
        Optional<User> optionalUser = userRepository.findById(addTeamChatMemberRequestDto.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        Optional<User> optionalNewMember = userRepository.findById(addTeamChatMemberRequestDto.getNewMemberId());

        if (optionalNewMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New member not found");
        }

        Optional<TeamChat> optionalTeamChat = teamChatRepository.findById(addTeamChatMemberRequestDto.getChatId());

        if (optionalTeamChat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        Optional<ChatMember> optionalChatMemberUser = chatMemberRepository.findByUserIdAndChatId(addTeamChatMemberRequestDto.getUserId(), addTeamChatMemberRequestDto.getChatId());

        if (optionalChatMemberUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of this chat");
        }

        Optional<ChatMember> optionalChatMemberNewMember = chatMemberRepository.findByUserIdAndChatId(addTeamChatMemberRequestDto.getNewMemberId(), addTeamChatMemberRequestDto.getChatId());

        if (!optionalChatMemberNewMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New member is already a member of this chat");
        }

        TeamChat teamChat = optionalTeamChat.get();

        teamChat.setLastUpdated(LocalDateTime.now());

        teamChatRepository.save(teamChat);

        ChatMember chatMember = new ChatMember();

        chatMember.setChatId(addTeamChatMemberRequestDto.getChatId());
        chatMember.setUserId(addTeamChatMemberRequestDto.getNewMemberId());

        chatMemberRepository.save(chatMember);

        return ResponseEntity.ok(chatMember);
    }

    public ResponseEntity<Object> removeTeamChatMember(RemoveTeamChatMemberRequestDto removeTeamChatMemberRequestDto) {
        Optional<User> optionalUser = userRepository.findById(removeTeamChatMemberRequestDto.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        Optional<User> optionalMember = userRepository.findById(removeTeamChatMemberRequestDto.getMemberId());

        if (optionalMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Member not found");
        }

        Optional<TeamChat> optionalTeamChat = teamChatRepository.findById(removeTeamChatMemberRequestDto.getChatId());

        if (optionalTeamChat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        Optional<ChatMember> optionalChatMemberUser = chatMemberRepository.findByUserIdAndChatId(removeTeamChatMemberRequestDto.getUserId(), removeTeamChatMemberRequestDto.getChatId());

        if (optionalChatMemberUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of this chat");
        }

        Optional<ChatMember> optionalChatMemberMember = chatMemberRepository.findByUserIdAndChatId(removeTeamChatMemberRequestDto.getMemberId(), removeTeamChatMemberRequestDto.getChatId());

        if (optionalChatMemberMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Person to remove is not a member of this chat");
        }

        TeamChat teamChat = optionalTeamChat.get();

        if (teamChat.getAdminId() == removeTeamChatMemberRequestDto.getMemberId()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Admin cannot be removed from the chat");
        }

        if (teamChat.getAdminId() != removeTeamChatMemberRequestDto.getUserId()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not the admin of this chat");
        }

        ChatMember chatMember = optionalChatMemberMember.get();

        chatMemberRepository.delete(chatMember);

        teamChat.setLastUpdated(LocalDateTime.now());

        teamChatRepository.save(teamChat);

        return ResponseEntity.ok("Member removed");
    }

    @Transactional
    public ResponseEntity<Object> deleteTeamChat(DeleteTeamChatRequestDto deleteTeamChatRequestDto) {
        Optional<User> optionalUser = userRepository.findById(deleteTeamChatRequestDto.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        Optional<TeamChat> optionalTeamChat = teamChatRepository.findById(deleteTeamChatRequestDto.getChatId());

        if (optionalTeamChat.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat not found");
        }

        TeamChat teamChat = optionalTeamChat.get();

        if (teamChat.getAdminId() != deleteTeamChatRequestDto.getUserId()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not the admin of this chat");
        }

        chatMemberRepository.deleteByChatId(deleteTeamChatRequestDto.getChatId());
        messageRepository.deleteByChatId(deleteTeamChatRequestDto.getChatId());
        teamChatRepository.deleteById(deleteTeamChatRequestDto.getChatId());

        return ResponseEntity.ok("Chat deleted");
    }
}