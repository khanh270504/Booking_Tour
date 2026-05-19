package com.example.bookingtour.dtos.request.crm;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeadCreateRequest {

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String source;

    private Integer interestedTourId;

    private Integer assignedStaffId;

    private String notes;
}