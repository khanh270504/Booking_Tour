package com.example.bookingtour.repositories.ChatMes;

import com.example.bookingtour.entities.ChatMessage.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMessageRepository
        extends MongoRepository<ChatMessage, String> {

    List<ChatMessage>
    findByConversationIdOrderByCreatedAtAsc(
            String conversationId
    );

    @Query("SELECT m FROM ChatMessage m WHERE m.createdAt = " +
            "(SELECT MAX(m2.createdAt) FROM ChatMessage m2 WHERE m2.conversationId = m.conversationId) " +
            "ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestMessagesPerRoom();
}
