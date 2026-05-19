package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.crm.InteractionCreateRequest;
import com.example.bookingtour.dtos.response.crm.InteractionResponse;

import java.util.List;

public interface IInteractionService {
    InteractionResponse logInteraction(InteractionCreateRequest request);
    List<InteractionResponse> getHistoryByLead(Integer leadId);
}
