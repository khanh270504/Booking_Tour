package com.example.bookingtour.entities.ChatMessage;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;

    private Long customerId;

    // admin đang support
    private Long adminId;

    // OPEN / CLOSED
    private String status;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private Integer unreadAdminCount;

    private Integer unreadCustomerCount;

    private LocalDateTime createdAt;
}