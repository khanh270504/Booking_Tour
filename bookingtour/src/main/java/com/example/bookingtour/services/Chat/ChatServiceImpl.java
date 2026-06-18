package com.example.bookingtour.services.Chat;

import com.example.bookingtour.IServices.IChatService;
import com.example.bookingtour.dtos.request.chatMes.ChatRequest;
import com.example.bookingtour.dtos.response.chatMes.ChatRoomResponse;
import com.example.bookingtour.entities.ChatMessage.ChatMessage;
import com.example.bookingtour.entities.ChatMessage.Conversation;
import com.example.bookingtour.entities.CustomerProfile;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.ChatMes.ChatMessageRepository;
import com.example.bookingtour.repositories.ChatMes.ConversationRepository;
import com.example.bookingtour.repositories.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements IChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @Override
    public ChatMessage saveMessage(ChatRequest request, Principal principal) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_MESSAGE_CONTENT);
        }

        Integer senderId = null;
        String senderRole = "GUEST";
        String guestId = request.getGuestId();
        String conversationId = request.getConversationId();

        if (conversationId != null) {
            conversationId = conversationId.trim().toLowerCase();
        }

        if (principal instanceof Authentication authentication && authentication.getPrincipal() instanceof Jwt jwt) {
            Object userId = jwt.getClaim("userId");
            if (userId != null) senderId = Integer.parseInt(userId.toString());

            String scope = jwt.getClaimAsString("scope");
            senderRole = (scope != null) ? scope.toUpperCase().replace("ROLE_", "") : "SALE";

            if ("CUSTOMER".equals(senderRole)) {
                conversationId = "customer_" + senderId;
            }
        } else {
            senderRole = "GUEST";
            if (guestId == null || guestId.trim().isEmpty()) {
                guestId = conversationId != null ? conversationId : "guest_" + UUID.randomUUID().toString().substring(0, 8);
            }
            conversationId = guestId.toLowerCase();
        }

        if (conversationId != null && conversationId.startsWith("guest_")) {
            guestId = conversationId;
        }

        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .senderRole(senderRole)
                .guestId(guestId)
                .content(request.getContent().trim())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessage savedMsg = chatMessageRepository.save(message);

        if (conversationId != null) {
            String finalConversationId = conversationId;
            String finalSenderRole = senderRole;
            Integer finalSenderId = senderId;
            String finalGuestId = guestId;

            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseGet(() -> {
                        Conversation newCon = Conversation.builder()
                                .id(finalConversationId)
                                .status("OPEN")
                                .createdAt(LocalDateTime.now())
                                .build();
                        if ("CUSTOMER".equals(finalSenderRole) && finalSenderId != null) {
                            newCon.setCustomerId(Long.valueOf(finalSenderId));
                        } else if (finalConversationId.startsWith("guest_")) {
                            newCon.setGuestId(finalGuestId);
                        }
                        return newCon;
                    });

            conversation.setLastMessage(savedMsg.getContent());
            conversation.setLastMessageAt(LocalDateTime.now());

            if ("CUSTOMER".equals(senderRole) || "GUEST".equals(senderRole)) {
                int currentUnread = conversation.getUnreadAdminCount() != null ? conversation.getUnreadAdminCount() : 0;
                conversation.setUnreadAdminCount(currentUnread + 1);
            }
            conversationRepository.save(conversation);
        }

        return savedMsg;
    }

    @Override
    public List<ChatMessage> getMessages(String conversationId, Principal principal) {
        if (conversationId != null) conversationId = conversationId.trim().toLowerCase();

        Integer currentUserId = null;
        String currentRole = "GUEST";

        if (principal instanceof Authentication authentication && authentication.getPrincipal() instanceof Jwt jwt) {
            Object uId = jwt.getClaim("userId");
            if (uId != null) currentUserId = Integer.parseInt(uId.toString());
            String scope = jwt.getClaimAsString("scope");
            currentRole = (scope != null) ? scope.toUpperCase().replace("ROLE_", "") : "GUEST";
        }

        if (!"ADMIN".equals(currentRole) && !"SALE".equals(currentRole)) {
            if ("CUSTOMER".equals(currentRole)) {
                String requiredRoom = "customer_" + currentUserId;
                if (!requiredRoom.equalsIgnoreCase(conversationId)) {
                    throw new AppException(ErrorCode.UNAUTHORIZED_CHAT_ACCESS);
                }
            } else if ("GUEST".equals(currentRole)) {
                if (conversationId == null || !conversationId.startsWith("guest_")) {
                    throw new AppException(ErrorCode.UNAUTHORIZED_CHAT_ACCESS);
                }
            }
        }

        conversationRepository.findById(conversationId).ifPresent(con -> {
            con.setUnreadAdminCount(0);
            conversationRepository.save(con);
        });

        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Override
    public List<ChatRoomResponse> getActiveRooms() {
        List<Conversation> activeConversations = conversationRepository.findAllByOrderByLastMessageAtDesc();

        List<Integer> customerIds = activeConversations.stream()
                .map(Conversation::getCustomerId)
                .filter(Objects::nonNull)
                .map(Long::intValue)
                .collect(Collectors.toList());

        Map<Integer, String> customerNameMap = new HashMap<>();
        if (!customerIds.isEmpty()) {
            List<CustomerProfile> profiles = customerProfileRepository.findByUser_IdIn(customerIds);
            customerNameMap = profiles.stream()
                    .filter(p -> p.getUser() != null && p.getFullName() != null)
                    .collect(Collectors.toMap(
                            p -> Integer.valueOf(p.getUser().getId().toString().trim()),
                            CustomerProfile::getFullName,
                            (v1, v2) -> v1
                    ));
        }

        Map<Integer, String> finalCustomerNameMap = customerNameMap;

        return activeConversations.stream().map(con -> {
            String conId = con.getId() != null ? con.getId().trim().toLowerCase() : "";
            String displayName = "Hành khách vãng lai";

            if (conId.startsWith("customer_")) {
                String customerIdStr = conId.replace("customer_", "").trim();
                Integer cId = Integer.parseInt(customerIdStr);

                if (finalCustomerNameMap.containsKey(cId)) {
                    displayName = finalCustomerNameMap.get(cId);
                } else {
                    displayName = "Thành viên #" + cId;
                }
            } else if (conId.startsWith("guest_")) {
                String cleanId = conId.replace("guest_", "").trim();
                if (cleanId.length() > 8) cleanId = cleanId.substring(0, 8);
                displayName = "Khách vãng lai #" + cleanId.toUpperCase();
            }

            return ChatRoomResponse.builder()
                    .conversationId(con.getId())
                    .guestName(displayName)
                    .lastMessage(con.getLastMessage() != null ? con.getLastMessage() : "Đã mở tab chờ hỗ trợ...")
                    .updatedAt(con.getLastMessageAt() != null ? con.getLastMessageAt() : con.getCreatedAt())
                    .unreadCount(con.getUnreadAdminCount())
                    .build();

        }).collect(Collectors.toList());
    }
}