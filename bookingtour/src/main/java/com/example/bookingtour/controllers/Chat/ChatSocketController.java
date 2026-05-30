package com.example.bookingtour.controllers.Chat;

import com.example.bookingtour.dtos.request.chatMes.ChatRequest;
import com.example.bookingtour.entities.ChatMessage.ChatMessage;
import com.example.bookingtour.services.Chat.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatServiceImpl chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(@Payload ChatRequest request, Principal principal) {

        ChatMessage result = chatService.saveMessage(request, principal);
        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.getConversationId(),
                result
        );
    }
}