package org.adz1q.nextmessagebackend.service;

import lombok.Data;
import org.adz1q.nextmessagebackend.model.*;
import org.adz1q.nextmessagebackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    private final MessageRepository messageRepository;
    private final SecretKey secretKey;
    private final PrivateChatRepository privateChatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(
            MessageRepository messageRepository,
            PrivateChatRepository privateChatRepository,
            ChatMemberRepository chatMemberRepository,
            UserRepository userRepository,
            ChatRepository chatRepository
    ) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);

        this.secretKey = keyGenerator.generateKey();
        this.messageRepository = messageRepository;
        this.privateChatRepository = privateChatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
    }

    @Data
    public static class EncryptedMessage {
        private String content;
        private SecretKey secretKey;
    }

    @Data
    public static class PrivateChatRequestDto {
        private int senderId;
        private int receiverId;
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

    public void saveMessage(ChatMessage chatMessage) {
        Message message = new Message();

        message.setChatId(chatMessage.getChatId());
        message.setSenderId(chatMessage.getSenderId());
        message.setContent(chatMessage.getContent());
        message.setDate(chatMessage.getDate());
        message.setSecretKey(chatMessage.getSecretKey());

        messageRepository.save(message);
    }

    public ChatMessage sendMessage(ChatMessage chatMessage) throws Exception {
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

        saveMessage(chatMessage);

        Chat chat = optionalChat.get();

        chat.setLastUpdated(LocalDateTime.now());
        chatRepository.save(chat);

        String decryptedContent = decryptMessage(chatMessage.getContent(), chatMessage.getSecretKey());
        chatMessage.setContent(decryptedContent);

        return chatMessage;
    }

    public List<Message> getMessages(int chatId, int userId, int offset, int limit) {
        Optional<ChatMember> optionalChatMember = chatMemberRepository.findByUserIdAndChatId(userId, chatId);

        if (optionalChatMember.isEmpty()) {
            throw new Error("User is not a member of this chat");
        }

        List<Message> messages = messageRepository.findByChatId(chatId, offset, limit);

        for (Message message : messages) {
            try {
                String decryptedContent = decryptMessage(message.getContent(), message.getSecretKey());
                message.setContent(decryptedContent);
            }
            catch (Exception error) {
                System.out.println("Error while decrypting message");
            }
        }

        return messages;
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
}