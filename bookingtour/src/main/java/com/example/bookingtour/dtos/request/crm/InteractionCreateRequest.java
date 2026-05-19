package com.example.bookingtour.dtos.request.crm;

import com.example.bookingtour.enums.InteractionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;

@Data
public class InteractionCreateRequest {
    @NotNull(message = "ID khách hàng tiềm năng không được trống")
    private Integer leadId;

    @NotNull(message = "Loại tương tác không được để trống")
    private InteractionType interactionType;

    private String status;

    private String note; 

    private Instant nextActionDate;
}