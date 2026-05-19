package com.example.bookingtour.entities.ChatMessage;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "messages")
public class ChatMessage {

    @Id
    private String id;

    private String conversationId;
    private String senderId;
    private String senderRole; // USER / ADMIN
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();
}
