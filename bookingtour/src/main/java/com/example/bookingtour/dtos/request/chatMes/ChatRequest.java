package com.example.bookingtour.dtos.request.chatMes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {
    private String conversationId;

    private String content;

    private String guestId;
}
