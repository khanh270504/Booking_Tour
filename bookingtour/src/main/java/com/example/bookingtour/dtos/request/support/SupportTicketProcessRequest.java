package com.example.bookingtour.dtos.request.support;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupportTicketProcessRequest {
    @NotNull(message = "Thiếu ID Ticket")
    private Integer ticketId;

    private String status; // PROCESSING, CLOSED

    private String responseMessage;
}