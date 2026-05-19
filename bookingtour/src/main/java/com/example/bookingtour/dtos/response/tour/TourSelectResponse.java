package com.example.bookingtour.dtos.response.tour;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourSelectResponse {
    private Integer id;
    private String name;
}