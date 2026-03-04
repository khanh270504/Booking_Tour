package com.example.bookingtour.dtos.request.support;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportTicketCreateRequest {
    private Integer bookingId;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String subject;

    @NotBlank(message = "Nội dung không được để trống")
    private String description;

    private String priority; // LOW, MEDIUM, HIGH
}