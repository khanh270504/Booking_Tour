package com.example.bookingtour.dtos.response.crm;

import lombok.Data;
import java.time.Instant;

@Data
public class LeadResponse {
    private String id;
    private String fullName;
    private String phone;
    private String email;
    private String source;
    private String status;
    private String interestedTourName;
    private String assignedStaffName;
    private Instant createdAt;
}