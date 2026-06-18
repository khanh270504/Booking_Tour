package com.example.bookingtour.IServices;


import com.example.bookingtour.dtos.request.chatMes.ChatRequest;
import com.example.bookingtour.dtos.response.chatMes.ChatRoomResponse;
import com.example.bookingtour.entities.ChatMessage.ChatMessage;

import java.security.Principal;
import java.util.List;

public interface IChatService {

    ChatMessage saveMessage(ChatRequest request, Principal principal);
    List<ChatMessage> getMessages(String conversationId, Principal principal);
    List<ChatRoomResponse> getActiveRooms();
}
