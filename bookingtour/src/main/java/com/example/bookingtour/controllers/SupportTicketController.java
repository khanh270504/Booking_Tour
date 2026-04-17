package com.example.bookingtour.controllers;

import com.example.bookingtour.dtos.request.support.SupportTicketCreateRequest;
import com.example.bookingtour.dtos.request.support.SupportTicketProcessRequest;
import com.example.bookingtour.dtos.response.support.SupportTicketResponse;
import com.example.bookingtour.enums.TicketStatus;
import com.example.bookingtour.services.ISupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        return Integer.parseInt(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<SupportTicketResponse> createTicket(
            @Valid @RequestBody SupportTicketCreateRequest request) {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(ticketService.createTicket(request, userId));
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<SupportTicketResponse>> getMyTickets() {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(ticketService.getMyTickets(userId));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<SupportTicketResponse>> getAllTickets(
            @RequestParam(required = false) TicketStatus status) {
        return ResponseEntity.ok(ticketService.getAllTicketsForAdmin(status));
    }

    @PutMapping("/admin/{id}/process")
    public ResponseEntity<SupportTicketResponse> processTicket(
            @PathVariable Integer id,
            @Valid @RequestBody SupportTicketProcessRequest request) {
        Integer adminId = getCurrentUserId();
        return ResponseEntity.ok(ticketService.processTicket(id, request, adminId));
    }
}