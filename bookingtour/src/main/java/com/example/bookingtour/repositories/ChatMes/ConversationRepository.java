package com.example.bookingtour.repositories.ChatMes;

import com.example.bookingtour.entities.ChatMessage.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByCustomerId(Long customerId);

    List<Conversation> findByAdminId(Long adminId);
    List<Conversation> findAllByOrderByLastMessageAtDesc();
}
