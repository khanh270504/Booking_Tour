package com.example.bookingtour.dtos.response.chatMes;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationResponse {

    private String id;

    private Integer customerId;

    private Integer adminId;

    private String status;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private Integer unreadAdminCount;

    private Integer unreadCustomerCount;
}
