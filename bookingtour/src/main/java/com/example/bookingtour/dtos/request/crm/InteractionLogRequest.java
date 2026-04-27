package com.example.bookingtour.dtos.request.crm;

import lombok.Data;
import java.time.Instant;

@Data
public class InteractionLogRequest {
    private String leadId;
    private String userId;

    private String interactionType;
    private String note;
    private Instant nextActionDate;
}