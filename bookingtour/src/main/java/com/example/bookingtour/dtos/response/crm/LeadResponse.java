package com.example.bookingtour.dtos.response.crm;

import com.example.bookingtour.entities.CrmLead;
import com.example.bookingtour.enums.LeadPriority;
import com.example.bookingtour.enums.LeadSource;
import com.example.bookingtour.enums.LeadStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
    private LeadSource source;
    private LeadStatus status;
    private LeadPriority priority;

    private Integer interestedTourId;
    private String interestedTourName;

    private Integer assignedStaffId;
    private String assignedStaffName;

    private Integer estimatedPeople;
    private BigDecimal estimatedBudget;

    private LocalDate expectedTravelDate;
    private Instant lastContactAt;
    private String notes;
    private String lostReason;
    private Instant createdAt;
    private Instant updatedAt;

    public static LeadResponse fromLead(CrmLead entity) {
        if (entity == null) return null;

        return LeadResponse.builder()
                .id(entity.getId())
                .leadCode(entity.getLeadCode())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .source(entity.getSource())
                .status(entity.getStatus())
                .priority(entity.getPriority())

                .interestedTourId(entity.getInterestedTour() != null ? entity.getInterestedTour().getId() : null)
                .interestedTourName(entity.getInterestedTour() != null ? entity.getInterestedTour().getName() : null)

                .assignedStaffId(entity.getAssignedStaff() != null ? entity.getAssignedStaff().getId() : null)
                .assignedStaffName(entity.getAssignedStaff() != null ? entity.getAssignedStaff().getFullName() : null)

                .estimatedPeople(entity.getEstimatedPeople())
                .estimatedBudget(entity.getEstimatedBudget())
                .expectedTravelDate(entity.getExpectedTravelDate())
                .lastContactAt(entity.getLastContactAt())
                .notes(entity.getNotes())
                .lostReason(entity.getLostReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}