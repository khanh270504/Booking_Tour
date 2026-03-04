package com.example.bookingtour.dtos.response.support;

import lombok.Data;
import java.time.Instant;

@Data
public class SupportTicketResponse {
    private Integer id;
    private String customerName;
    private Integer bookingId;
    private String subject;
    private String description;
    private String priority;
    private String status;
    private String assignedStaffName;
    private Instant createdAt;
}