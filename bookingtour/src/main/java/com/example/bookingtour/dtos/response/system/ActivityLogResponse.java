package com.example.bookingtour.dtos.response.system;

import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
public class ActivityLogResponse {
    private Integer id;
    private String userEmail; // Ai thao tác
    private String action;
    private String targetTable;
    private String targetId;
    private Map<String, Object> oldValue; // JSON
    private Map<String, Object> newValue; // JSON
    private Instant createdAt;
}