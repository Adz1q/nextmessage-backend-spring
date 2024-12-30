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
    public static class PrivateChatRequestDto {
        private int senderId;
        private int receiverId;
    }

    public String encryptMessage(String content) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedContent = cipher.doFinal(content.getBytes());

        return Base64.getEncoder().encodeToString(encryptedContent);
    }

    public String decryptMessage(String encryptedContent) throws Exception {
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

        messageRepository.save(message);
    }

    public ChatMessage sendMessage(ChatMessage chatMessage) throws Exception {
        Optional<Chat> optionalChat = chatRepository.findById(chatMessage.getChatId());

        if (optionalChat.isEmpty()) {
            throw new Exception("Chat not found");
        }

        String encryptedContent = encryptMessage(chatMessage.getContent());

        chatMessage.setContent(encryptedContent);
        chatMessage.setDate(LocalDateTime.now());

        saveMessage(chatMessage);

        Chat chat = optionalChat.get();

        chat.setLastUpdated(LocalDateTime.now());
        chatRepository.save(chat);

        return chatMessage;
    }

    public List<Message> getMessages(int chatId, int offset, int limit) {
        return messageRepository.findByChatId(chatId, offset, limit);
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