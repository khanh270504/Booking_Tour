package com.example.bookingtour.dtos.response.tour;

import com.example.bookingtour.entities.Tour;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourResponse {
    private Integer id;
    private String name;
    private String tourType;
    private String description;
    private String status;
    private String destinationName;
    private List<Map<String, Object>> itinerary;
    private List<String> imageUrls;
    private Instant createdAt;

    public static TourResponse fromTour(Tour tour) {
        if (tour == null) return null;

        return TourResponse.builder()
                .id(tour.getId())
                .name(tour.getName())
                .tourType(tour.getTourType())
                .description(tour.getDescription())
                .status(tour.getStatus().name())
                .destinationName(tour.getDestination() != null ? tour.getDestination().getName() : null)
                .createdAt(tour.getCreatedAt())
                .itinerary(tour.getItinerary())

                .imageUrls(tour.getTourImages() != null ?
                        tour.getTourImages().stream()
                                .map(image -> image.getImageUrl()) // Đảm bảo class TourImage có trường imageUrl nhé!
                                .collect(Collectors.toList())
                        : null)
                .build();
    }
}