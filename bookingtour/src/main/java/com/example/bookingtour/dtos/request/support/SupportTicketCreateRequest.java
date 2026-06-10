package com.example.bookingtour.dtos.request.support;

import com.example.bookingtour.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketCreateRequest {
    private String bookingCode;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String subject;

    @NotBlank(message = "Nội dung không được để trống")
    private String description;

    private TicketPriority priority;
}