package com.example.bookingtour.controllers;

import com.example.bookingtour.dtos.request.support.SupportTicketCreateRequest;
import com.example.bookingtour.dtos.request.support.SupportTicketProcessRequest;
import com.example.bookingtour.dtos.response.support.SupportTicketResponse;
import com.example.bookingtour.enums.TicketStatus;
import com.example.bookingtour.IServices.ISupportTicketService;
import com.example.bookingtour.dtos.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support-tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final ISupportTicketService ticketService;

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Vui lòng đăng nhập để thực hiện chức năng này");
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object userIdObj = jwtAuth.getToken().getClaims().get("userId");
            if (userIdObj != null) {
                return ((Number) userIdObj).intValue();
            }
        }

        throw new RuntimeException("Token không hợp lệ hoặc thiếu thông tin ID");
    }


    @PostMapping
    // @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<SupportTicketResponse> createTicket(
            @Valid @RequestBody SupportTicketCreateRequest request) {
        Integer userId = getCurrentUserId();
        SupportTicketResponse data = ticketService.createTicket(request, userId);

        return ApiResponse.<SupportTicketResponse>builder()
                .code(200) // Hoặc tùy cấu trúc ApiResponse của bạn
                .message("Tạo yêu cầu hỗ trợ thành công")
                .result(data)
                .build();
    }

    @GetMapping("/my-tickets")
    // @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<List<SupportTicketResponse>> getMyTickets() {
        Integer userId = getCurrentUserId();
        return ApiResponse.<List<SupportTicketResponse>>builder()
                .code(200)
                .result(ticketService.getMyTickets(userId))
                .build();
    }

    @PostMapping("/admin/create-for-customer")
    // @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    public ApiResponse<SupportTicketResponse> createTicketByAdmin(
            @Valid @RequestBody SupportTicketCreateRequest request) {

        SupportTicketResponse data = ticketService.createTicketByAdmin(request);

        return ApiResponse.<SupportTicketResponse>builder()
                .code(200)
                .message("Tạo Ticket hỗ trợ thành công")
                .result(data)
                .build();
    }

    @GetMapping("/admin/all")
    // @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<SupportTicketResponse>> getAllTickets(
            @RequestParam(required = false) TicketStatus status) {

        return ApiResponse.<List<SupportTicketResponse>>builder()
                .code(200)
                .result(ticketService.getAllTicketsForAdmin(status))
                .build();
    }

    @PutMapping("/admin/{id}/process")
    // @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    public ApiResponse<SupportTicketResponse> processTicket(
            @PathVariable Integer id,
            @Valid @RequestBody SupportTicketProcessRequest request) {

        Integer adminId = getCurrentUserId();
        SupportTicketResponse data = ticketService.processTicket(id, request, adminId);

        return ApiResponse.<SupportTicketResponse>builder()
                .code(200)
                .message("Xử lý Ticket thành công")
                .result(data)
                .build();
    }
}