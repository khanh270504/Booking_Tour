package com.example.bookingtour.dtos.response.tour;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SurchargeResponse {
    private Integer id;
    private String surchargeName;
    private BigDecimal amount;
    private Boolean isMandatory;
}