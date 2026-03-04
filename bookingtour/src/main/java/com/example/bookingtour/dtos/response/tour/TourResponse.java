package com.example.bookingtour.dtos.response.tour;

import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class TourResponse {
    private Integer id;
    private String name;
    private String tourType;
    private String description;
    private String status;
    private String destinationName;

    // List JSON Map cho Frontend vẽ Timeline lịch trình
    private List<Map<String, Object>> itinerary;

    // Gắn luôn danh sách URL ảnh để FE làm slider
    private List<String> imageUrls;

    private Instant createdAt;
}