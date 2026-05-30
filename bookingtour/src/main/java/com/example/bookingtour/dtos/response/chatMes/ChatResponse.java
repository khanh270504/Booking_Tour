package com.example.bookingtour.dtos.response.chatMes;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatResponse {

    private String id;

    private String conversationId;

    private Integer senderId;

    private String senderRole;

    private String content;

    private Boolean isRead;

    private LocalDateTime createdAt;
}