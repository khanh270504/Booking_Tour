package com.example.bookingtour.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private long totalElements;

    private List<T> data;
}