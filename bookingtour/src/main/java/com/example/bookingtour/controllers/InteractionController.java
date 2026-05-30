package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IInteractionService;
import com.example.bookingtour.dtos.request.crm.InteractionCreateRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crm/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final IInteractionService interactionService;

    @PostMapping
    public ApiResponse<?> logInteraction(@Valid @RequestBody InteractionCreateRequest request) {
        // staffId đã được bóc từ JWT trong Service nên không cần truyền ở đây
        return ApiResponse.builder()
                .code(200)
                .result(interactionService.logInteraction(request))
                .build();
    }

    @GetMapping("/lead/{leadId}")
    public ApiResponse<?> getHistory(@PathVariable Integer leadId) {
        return ApiResponse.builder()
                .code(200)
                .result(interactionService.getHistoryByLead(leadId))
                .build();
    }
}