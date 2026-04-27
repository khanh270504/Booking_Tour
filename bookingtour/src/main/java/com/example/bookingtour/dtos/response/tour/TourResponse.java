package com.example.bookingtour.dtos.response.tour;

import com.example.bookingtour.entities.Tour;
import com.example.bookingtour.entities.TourImage;
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
    private String description;
    private String status;
    private String destinationName;
    private List<Map<String, Object>> itinerary;
    private Instant createdAt;

    private String thumbnail;
    private String duration;
    private Integer minParticipants;
    private List<String> imageUrls;
    private List<ScheduleResponse> schedules;
    private List<PricingConfigResponse> pricings;
    public static TourResponse fromTour(Tour tour) {
        if (tour == null) return null;

        String calculatedDuration = "Liên hệ";
        if (tour.getItinerary() != null && !tour.getItinerary().isEmpty()) {
            int days = tour.getItinerary().size();
            int nights = days - 1;
            calculatedDuration = (days > 1) ? (days + " ngày " + nights + " đêm") : "Trong ngày";
        }
        return TourResponse.builder()
                .id(tour.getId())
                .name(tour.getName())
                .description(tour.getDescription())
                .status(tour.getStatus().name())
                .destinationName(tour.getDestination() != null ? tour.getDestination().getName() : null)
                .createdAt(tour.getCreatedAt())
                .itinerary(tour.getItinerary())

                .thumbnail(tour.getThumbnail())
                .duration(calculatedDuration)
                .minParticipants(tour.getMinParticipants())

                .imageUrls(tour.getTourImages() != null ?
                        tour.getTourImages().stream()
                                .map(TourImage::getImageUrl)
                                .collect(Collectors.toList())
                        : null)
                .build();
    }
}