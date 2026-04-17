package com.example.bookingtour.dtos.request.support;

import com.example.bookingtour.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupportTicketProcessRequest {

    private TicketStatus status; // PROCESSING, CLOSED

    private String responseMessage;
}