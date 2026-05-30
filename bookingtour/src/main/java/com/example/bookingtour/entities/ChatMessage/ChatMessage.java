package com.example.bookingtour.entities.ChatMessage;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "messages")
public class ChatMessage {

    @Id
    private String id;

    private String conversationId;

    private Integer senderId;

    private String senderRole;

    private String guestId;

    private String content;

    private Boolean isRead;

    private LocalDateTime createdAt;
}