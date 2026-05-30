package com.example.bookingtour.services.Chat;

import com.example.bookingtour.IServices.IChatService;
import com.example.bookingtour.dtos.request.chatMes.ChatRequest;
import com.example.bookingtour.dtos.response.chatMes.ChatRoomResponse;
import com.example.bookingtour.entities.ChatMessage.ChatMessage;
import com.example.bookingtour.entities.CustomerProfile;
import com.example.bookingtour.entities.User;
import com.example.bookingtour.repositories.ChatMes.ChatMessageRepository;
import com.example.bookingtour.repositories.CustomerProfileRepository;
import com.example.bookingtour.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final CustomerProfileRepository customerProfileRepository;
    @Override
    public ChatMessage saveMessage(ChatRequest request, Principal principal) {
        Integer senderId = null;
        String senderRole = "GUEST";
        String guestId = null;

        if (principal instanceof Authentication authentication && authentication.getPrincipal() instanceof Jwt jwt) {
            Object userId = jwt.getClaim("userId");
            if (userId != null) senderId = Integer.parseInt(userId.toString());

            String scope = jwt.getClaimAsString("scope");
            senderRole = (scope != null) ? scope.replace("ROLE_", "") : "STAFF";

            // Khi Admin rep tin nhắn, phải lấy đúng guestId của phòng chat đó từ request gửi lên
            guestId = request.getGuestId();
        }
        // Luồng 2: Khách vãng lai (GUEST không có Token)
        else {
            guestId = request.getGuestId();

            //  Nếu FE gửi lên trống (null), BE tự đẻ UUID để băm phòng, không lo chung chạ
            if (guestId == null || guestId.trim().isEmpty()) {
                guestId = "GUEST_" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }
        }

        ChatMessage message = ChatMessage.builder()
                .conversationId(request.getConversationId())
                .senderId(senderId)
                .senderRole(senderRole)
                .guestId(guestId)
                .content(request.getContent())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return chatMessageRepository.save(message);
    }

    @Override
    public List<ChatMessage> getMessages(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Override
    public List<ChatRoomResponse> getActiveRooms() {
        // 1. Lấy ra danh sách các tin nhắn cuối cùng của mỗi phòng
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesPerRoom();

        // 2. Duyệt qua từng phòng để phân loại tên hiển thị cho Admin
        return latestMessages.stream().map(m -> {
            String conId = m.getConversationId();
            String displayName = "Người dùng ẩn danh";

            //  TRƯỜNG HỢP 1: LUỒNG KHÔNG CÓ GUEST (Người dùng đã Đăng nhập)
            if (conId != null && conId.startsWith("customer_")) {
                String customerId = conId.replace("customer_", "");
                displayName = "Thành viên #" + customerId;


                CustomerProfile customerProfile = customerProfileRepository.findByUser_Id(Integer.parseInt(customerId)).orElse(null);
                if (customerProfile != null) displayName = customerProfile.getFullName();

            }

            // TRƯỜNG HỢP 2: LUỒNG CÓ GUEST (Khách vãng lai)
            else if (conId != null && conId.startsWith("guest_")) {
                // Lấy cái mã chuỗi UUID ra để hiển thị ngắn gọn cho đẹp
                String rawId = m.getGuestId() != null ? m.getGuestId() : conId;
                String shortId = rawId.replace("guest_", "");
                if (shortId.length() > 8) {
                    shortId = shortId.substring(0, 8); // Chỉ lấy 8 ký tự đầu cho gọn giao diện
                }
                displayName = "Khách vãng lai #" + shortId.toUpperCase();
            }

            return ChatRoomResponse.builder()
                    .conversationId(conId)
                    .guestName(displayName)
                    .lastMessage(m.getContent())
                    .updatedAt(m.getCreatedAt())
                    .build();

        }).collect(Collectors.toList());
    }
}