package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.tour.*;
import com.example.bookingtour.dtos.response.PageResponse;
import com.example.bookingtour.dtos.response.tour.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ITourService {

    TourResponse createTour(TourCreateRequest request);

    List<TourResponse> getAllTours();

    List<TourResponse> getAllToursForClient();

    TourResponse getTourByIdForClient(Integer id);

    TourResponse updateTour(Integer id, TourCreateRequest request);

    void deleteTour(Integer id);

    List<DestinationResponse> getAllDestinations();

    List<TourImageResponse> getImagesByTour(Integer tourId);

    PageResponse<TourResponse> searchTours(TourSearchRequest searchRequest, Pageable pageable);
}