package com.example.bookingtour.dtos.request.operation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProviderRequest {
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;

    @NotBlank(message = "Loại dịch vụ không được để trống")
    private String serviceType;

    private String contactInfo;
}