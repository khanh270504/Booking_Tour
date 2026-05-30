package com.example.bookingtour.dtos.request.crm;

import com.example.bookingtour.enums.LeadPriority;
import com.example.bookingtour.enums.LeadSource;
import com.example.bookingtour.enums.LeadStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class LeadUpdateRequest {
    private String fullName;
    private String phone;
    private String email;
    private LeadSource source;

    private LeadStatus status;
    private LeadPriority priority;


    private Integer interestedTourId;
    private LocalDate expectedTravelDate;

    private Integer estimatedPeople;
    private BigDecimal estimatedBudget;
    private String notes;
    private Integer assignedStaffId;
    private String lostReason;
}