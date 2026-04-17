package com.example.bookingtour.dtos.response.support;

import com.example.bookingtour.entities.SupportTicket;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class SupportTicketResponse {
    private Integer id;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Integer bookingId;
    private String subject;
    private String description;
    private String adminResponse;
    private String priority;
    private String status;
    private String assignedStaffName;
    private Instant createdAt;
    private Instant closedAt;

    public static SupportTicketResponse fromSupportTicket(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .customerName(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : null)

                .customerPhone(ticket.getCustomer() != null ? ticket.getCustomer().getPhone() : null)
                .customerEmail((ticket.getCustomer() != null && ticket.getCustomer().getUser() != null)
                        ? ticket.getCustomer().getUser().getEmail()
                        : null)
                .bookingId(ticket.getBooking() != null ? ticket.getBooking().getId() : null)
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .adminResponse(ticket.getAdminResponse())
                .priority(ticket.getPriority() != null ? ticket.getPriority().name() : null)
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : null)
                .assignedStaffName(ticket.getAssignedStaff() != null ? ticket.getAssignedStaff().getFullName() : "Chưa có người nhận")
                .createdAt(ticket.getCreatedAt())
                .closedAt(ticket.getClosedAt())
                .build();
    }
}