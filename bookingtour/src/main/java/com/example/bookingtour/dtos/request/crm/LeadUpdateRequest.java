package com.example.bookingtour.dtos.request.crm;

import com.example.bookingtour.enums.LeadStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class LeadUpdateRequest {
    private String fullName;
    private String email;
    private LeadStatus status;
    private String priority;
    private Integer estimatedPeople;
    private BigDecimal estimatedBudget;
    private Instant nextFollowUpAt;
    private String notes;
    private Integer assignedStaffId;
}