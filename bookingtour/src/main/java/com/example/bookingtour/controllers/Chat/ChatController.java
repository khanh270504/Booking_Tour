package com.example.bookingtour.controllers.Chat;

import com.example.bookingtour.dtos.request.chatMes.ChatRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.dtos.response.chatMes.ChatRoomResponse;
import com.example.bookingtour.entities.ChatMessage.ChatMessage;
import com.example.bookingtour.services.Chat.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatServiceImpl chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ApiResponse<ChatMessage> sendMessage(
            @RequestBody ChatRequest request,
            Principal principal
    ) {

        ChatMessage result = chatService.saveMessage(request, principal);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.getConversationId(),
                result
        );
        messagingTemplate.convertAndSend("/topic/admin/chat/notify", result);

        return ApiResponse.<ChatMessage>builder()
                .code(1000)
                .message("Gửi tin nhắn thành công")
                .result(result)
                .build();
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<List<ChatMessage>> getMessages(
            @PathVariable String conversationId
    ) {

        List<ChatMessage> result = chatService.getMessages(conversationId);

        return ApiResponse.<List<ChatMessage>>builder()
                .code(1000)
                .message("Nhận tin nhắn thành công")
                .result(result)
                .build();
    }
    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomResponse>> getActiveRooms() {
        return ApiResponse.<List<ChatRoomResponse>>builder()
                .code(1000)
                .message("Lấy danh sách phòng chat thành công")
                .result(chatService.getActiveRooms())
                .build();
    }
}