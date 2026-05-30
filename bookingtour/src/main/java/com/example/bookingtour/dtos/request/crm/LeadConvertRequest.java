package com.example.bookingtour.dtos.request.crm;

import lombok.Data;
@Data
public class LeadConvertRequest {
    private Integer adultCount;
    private Integer childCount;
    private Integer infantCount;
    private String voucherCode;
}