package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.booking.ReviewCreateRequest;
import com.example.bookingtour.dtos.request.support.SupportTicketCreateRequest;
import com.example.bookingtour.dtos.request.support.SupportTicketProcessRequest;
import com.example.bookingtour.dtos.response.support.ReviewResponse;
import com.example.bookingtour.dtos.response.support.SupportTicketResponse;

public interface ISupportService {

    ReviewResponse createReview(ReviewCreateRequest request, String userId);

    SupportTicketResponse createTicket(SupportTicketCreateRequest request, String userId);
    SupportTicketResponse processTicket(SupportTicketProcessRequest request, String staffId);
}