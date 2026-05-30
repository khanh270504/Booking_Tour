package com.example.bookingtour.dtos.response.crm;

import com.example.bookingtour.entities.CrmInteraction;
import com.example.bookingtour.enums.InteractionResult;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractionResponse {
    private Integer id;
    private String staffName;
    private String interactionType;
    private InteractionResult result;
    private String note;
    private Instant nextActionDate;
    private Instant createdAt;

    public static InteractionResponse fromInteractionResponse(CrmInteraction crmInteraction) {
        if (crmInteraction == null) return null;

        return InteractionResponse.builder()
                .id(crmInteraction.getId())


                .staffName(crmInteraction.getStaff() != null ? crmInteraction.getStaff().getFullName() : "Hệ thống")


                .interactionType(crmInteraction.getInteractionType() != null ? crmInteraction.getInteractionType().name() : null)
                .note(crmInteraction.getNote())

                .result(crmInteraction.getResult())

                .nextActionDate(crmInteraction.getNextActionDate())

                .createdAt(crmInteraction.getCreatedAt())
                .build();
    }
}