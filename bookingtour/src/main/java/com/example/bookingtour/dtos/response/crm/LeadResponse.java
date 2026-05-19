package com.example.bookingtour.dtos.response.crm;

import com.example.bookingtour.entities.CrmLead;
import com.example.bookingtour.enums.LeadStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadResponse {
    private Integer id;
    private String leadCode;
    private String fullName;
    private String phone;
    private String email;
    private String source;
    private String status;
    private String priority;
    private String interestedTourName;
    private String assignedStaffName;
    private BigDecimal estimatedBudget;
    private Instant nextFollowUpAt;
    private String notes;

    public static LeadResponse fromLead(CrmLead entity) {
        return LeadResponse.builder()
                .id(entity.getId())
                .leadCode(entity.getLeadCode())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .source(entity.getSource())
                .status(entity.getStatus().name())
                .priority(entity.getPriority())
                .interestedTourName(entity.getInterestedTour() != null ? entity.getInterestedTour().getName() : null)
                .assignedStaffName(entity.getAssignedStaff() != null ? entity.getAssignedStaff().getFullName() : null)
                .estimatedBudget(entity.getEstimatedBudget())
                .nextFollowUpAt(entity.getNextFollowUpAt())
                .notes(entity.getNotes())
                .build();
    }
}