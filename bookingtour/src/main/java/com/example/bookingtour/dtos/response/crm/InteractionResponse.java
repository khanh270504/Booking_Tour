package com.example.bookingtour.dtos.response.crm;

import com.example.bookingtour.entities.CrmInteraction;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractionResponse {
    private Integer id;
    private String staffName;
    private String staffAvatar;
    private String interactionType;
    private String status;
    private String note;
    private Instant nextActionDate;
    private Instant createdAt;

    public static InteractionResponse fromInteractionResponse(CrmInteraction crmInteraction) {
        return InteractionResponse.builder()
                .id(crmInteraction.getId())
                .staffName(crmInteraction.getStaff().getFullName())
                .interactionType(crmInteraction.getInteractionType().name())
                .build();
    }
}