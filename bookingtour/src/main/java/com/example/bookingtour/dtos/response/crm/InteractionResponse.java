package com.example.bookingtour.dtos.response.crm;

import lombok.Data;
import java.time.Instant;

@Data
public class InteractionResponse {
    private Integer id;
    private String staffName;
    private String interactionType;
    private String note;
    private Instant nextActionDate;
    private Instant createdAt;
}