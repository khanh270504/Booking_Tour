package com.example.bookingtour.dtos.request.support;

import com.example.bookingtour.enums.TicketPriority;
import com.example.bookingtour.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketProcessRequest {

    private TicketStatus status;
    private TicketPriority priority;
    private String responseMessage;
}