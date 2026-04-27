package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.support.SupportTicketCreateRequest;
import com.example.bookingtour.dtos.request.support.SupportTicketProcessRequest;
import com.example.bookingtour.dtos.response.support.SupportTicketResponse;
import com.example.bookingtour.enums.TicketStatus;

import java.util.List;

public interface ISupportTicketService {
    SupportTicketResponse createTicket(SupportTicketCreateRequest request, Integer userId);
    List<SupportTicketResponse> getMyTickets(Integer userId);
    List<SupportTicketResponse> getAllTicketsForAdmin(TicketStatus status);
    SupportTicketResponse processTicket(Integer ticketId, SupportTicketProcessRequest request, Integer adminId);
}