package com.example.bookingtour.dtos.response.operation;

import lombok.Data;

@Data
public class ProviderResponse {
    private Integer id;
    private String name;
    private String serviceType;
    private String contactInfo;
}