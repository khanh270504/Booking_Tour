package com.example.bookingtour.services;

import com.example.bookingtour.IServices.ITourCostService;
import com.example.bookingtour.dtos.request.operation.TourCostRequest;
import com.example.bookingtour.dtos.response.operation.TourCostResponse;
import com.example.bookingtour.entities.Provider;
import com.example.bookingtour.entities.TourCost;
import com.example.bookingtour.entities.TourSchedule;
import com.example.bookingtour.enums.TourCostStatus;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.ProviderRepository;
import com.example.bookingtour.repositories.TourCostRepository;
import com.example.bookingtour.repositories.TourScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourCostServiceImpl implements ITourCostService {

    private final TourCostRepository tourCostRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final ProviderRepository providerRepository;

    @Override
    @Transactional
    public TourCostResponse createTourCost(TourCostRequest request) {
        // 1. Validate tồn tại
        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND));

        // 2. Build Entity
        TourCost tourCost = TourCost.builder()
                .schedule(schedule)
                .provider(provider)
                .expenseName(request.getExpenseName())
                .amount(request.getAmount())
                .status(TourCostStatus.valueOf(request.getStatus().toUpperCase()))
                .note(request.getNote())
                .paidAt(request.getPaidAt())
                .build();

        // 3. Save và trả về DTO
        TourCost savedCost = tourCostRepository.save(tourCost);
        return TourCostResponse.fromEntity(savedCost);
    }

    @Override
    @Transactional
    public TourCostResponse updateTourCost(Integer id, TourCostRequest request) {
        TourCost tourCost = tourCostRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_COST_NOT_FOUND));

        // Cập nhật Provider nếu đổi
        if (!tourCost.getProvider().getId().equals(request.getProviderId())) {
            Provider provider = providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND));
            tourCost.setProvider(provider);
        }

        tourCost.setExpenseName(request.getExpenseName());
        tourCost.setAmount(request.getAmount());
        tourCost.setStatus(TourCostStatus.valueOf(request.getStatus().toUpperCase()));
        tourCost.setNote(request.getNote());
        tourCost.setPaidAt(request.getPaidAt());

        tourCostRepository.save(tourCost);
        return TourCostResponse.fromEntity(tourCost);
    }

    @Override
    public List<TourCostResponse> getTourCostsByScheduleId(Integer scheduleId) {
        // Sếp nhớ tạo hàm findByScheduleId trong TourCostRepository nhé
        return tourCostRepository.findByScheduleId(scheduleId).stream()
                .map(TourCostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public TourCostResponse getTourCostById(Integer id) {
        TourCost tourCost = tourCostRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_COST_NOT_FOUND));
        return TourCostResponse.fromEntity(tourCost);
    }

    @Override
    @Transactional
    public TourCostResponse updateCostStatus(Integer id, String status, String note) {
        TourCost tourCost = tourCostRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_COST_NOT_FOUND));

        TourCostStatus newStatus = TourCostStatus.valueOf(status.toUpperCase());
        tourCost.setStatus(newStatus);

        if (note != null && !note.isEmpty()) {
            tourCost.setNote(note);
        }

        // Tự động set Ngày thanh toán nếu Kế toán chuyển status thành PAID
        if (newStatus == TourCostStatus.PAID && tourCost.getPaidAt() == null) {
            tourCost.setPaidAt(Instant.now());
        }

        tourCostRepository.save(tourCost);
        return TourCostResponse.fromEntity(tourCost);
    }

    @Override
    @Transactional
    public void deleteTourCost(Integer id) {
        if (!tourCostRepository.existsById(id)) {
            throw new AppException(ErrorCode.TOUR_COST_NOT_FOUND);
        }
        tourCostRepository.deleteById(id);
    }
}