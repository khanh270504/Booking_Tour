package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IInteractionService;
import com.example.bookingtour.dtos.request.crm.InteractionCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crm/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final IInteractionService interactionService;

    @PostMapping
    public ResponseEntity<?> logInteraction(@Valid @RequestBody InteractionCreateRequest request) {
        // staffId đã được bóc từ JWT trong Service nên không cần truyền ở đây
        return ResponseEntity.status(HttpStatus.CREATED).body(interactionService.logInteraction(request));
    }

    @GetMapping("/lead/{leadId}")
    public ResponseEntity<?> getHistory(@PathVariable Integer leadId) {
        return ResponseEntity.ok(interactionService.getHistoryByLead(leadId));
    }
}