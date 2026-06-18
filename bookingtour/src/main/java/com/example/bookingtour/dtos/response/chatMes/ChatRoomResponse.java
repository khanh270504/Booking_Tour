package com.example.bookingtour.dtos.response.chatMes;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponse {
    private String conversationId;
    private String guestName;
    private String lastMessage;
    private LocalDateTime updatedAt;
    private Integer unreadCount;
}