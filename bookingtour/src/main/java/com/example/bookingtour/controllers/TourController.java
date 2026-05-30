package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.IScheduleService;
import com.example.bookingtour.IServices.ITourService;
import com.example.bookingtour.dtos.request.tour.*;
import com.example.bookingtour.dtos.response.ApiResponse;
import com.example.bookingtour.dtos.response.PageResponse;
import com.example.bookingtour.dtos.response.tour.*;
import com.example.bookingtour.repositories.TourRepository;
import com.example.bookingtour.services.TourImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TourController {

    private final ITourService tourService;
    private final IScheduleService scheduleService;
    private final TourRepository tourRepository;
    private final TourImageService tourImageService;
    @GetMapping("/tours")
    public ApiResponse<List<TourResponse>> getAllToursForClient() {
        return ApiResponse.<List<TourResponse>>builder()
                .result(tourService.getAllToursForClient())
                .build();
    }

    @GetMapping("/tours/{id}")
    public ApiResponse<TourResponse> getTourDetailForClient(@PathVariable Integer id) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.getTourByIdForClient(id))
                .build();
    }

    @GetMapping("/destinations")
    public ApiResponse<List<DestinationResponse>> getAllDestinations() {
        return ApiResponse.<List<DestinationResponse>>builder()
                .result(tourService.getAllDestinations())
                .build();
    }

    // Lấy bảng giá của tour (để khách xem người lớn/trẻ em bao nhiêu tiền)
    @GetMapping("/tours/{id}/pricing")
    public ApiResponse<List<PricingConfigResponse>> getTourPricing(@PathVariable Integer id) {
        return ApiResponse.<List<PricingConfigResponse>>builder()
                .result(scheduleService.getPricingBySchedule(id))
                .build();
    }

    // Lấy lịch khởi hành để khách chọn ngày đi
    @GetMapping("/tours/{id}/schedules")
    public ApiResponse<List<ScheduleResponse>> getTourSchedules(@PathVariable Integer id) {
        return ApiResponse.<List<ScheduleResponse>>builder()
                .result(scheduleService.getSchedulesByTourId(id))
                .build();
    }

    @GetMapping("/schedules/{id}")
    public ApiResponse<ScheduleResponse> getScheduleDetail(@PathVariable Integer id) {
        return ApiResponse.<ScheduleResponse>builder()
                .result(scheduleService.getScheduleById(id))
                .build();
    }


    @PostMapping("/admin/tours")
    public ApiResponse<TourResponse> createTour(@RequestBody @Valid TourCreateRequest request) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.createTour(request))
                .build();
    }

    @PutMapping("/admin/tours/{id}")
    public ApiResponse<TourResponse> updateTour(@PathVariable Integer id, @RequestBody @Valid TourCreateRequest request) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.updateTour(id, request))
                .build();
    }

    @GetMapping("/admin/tours")
    public ApiResponse<List<TourResponse>> getAllToursForAdmin() {
        return ApiResponse.<List<TourResponse>>builder()
                .result(tourService.getAllTours())
                .build();
    }

    @DeleteMapping("/admin/tours/{id}")
    public ApiResponse<String> deleteTour(@PathVariable Integer id) {
        tourService.deleteTour(id);
        return ApiResponse.<String>builder()
                .result("Tour đã được chuyển sang trạng thái INACTIVE thành công")
                .build();
    }
    @PatchMapping("/admin/tours/{id}/restore")
    public ApiResponse<String> restoreTour(@PathVariable Integer id) {
        tourService.restoreTour(id);
        return ApiResponse.<String>builder()
                .result("Khôi phục tour thành công!")
                .build();
    }

    // Quản lý Lịch trình (Schedules)
    @PostMapping("/admin/tours/schedules")
    public ApiResponse<ScheduleResponse> createSchedule(@RequestBody @Valid ScheduleCreateRequest request) {
        return ApiResponse.<ScheduleResponse>builder()
                .result(scheduleService.createSchedule(request))
                .build();
    }

    // Cập nhật trạng thái lịch trình (Đóng/Mở/Hủy)
    @PatchMapping("/admin/schedules/{id}/status")
    public ApiResponse<ScheduleResponse> updateScheduleStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        return ApiResponse.<ScheduleResponse>builder()
                .result(scheduleService.updateScheduleStatus(id, status))
                .build();
    }

    // Quản lý Giá (Pricing)
    @PostMapping("/admin/tours/pricing")
    public ApiResponse<PricingConfigResponse> createPricing(@RequestBody @Valid PricingConfigRequest request) {
        return ApiResponse.<PricingConfigResponse>builder()
                .result(scheduleService.createPricing(request))
                .build();
    }

    // Quản lý Phụ phí (Surcharges)
    @PostMapping("/admin/tours/surcharges")
    public ApiResponse<SurchargeResponse> createSurcharge(@RequestBody @Valid SurchargeRequest request) {
        return ApiResponse.<SurchargeResponse>builder()
                .result(scheduleService.createSurcharge(request))
                .build();
    }

    @GetMapping("/admin/tours/{id}/surcharges")
    public ApiResponse<List<SurchargeResponse>> getSurchargesByTour(@PathVariable Integer id) {
        return ApiResponse.<List<SurchargeResponse>>builder()
                .result(scheduleService.getSurchargesBySchedule(id))
                .build();
    }
    @GetMapping("/tours/search")
    public ApiResponse<PageResponse<TourResponse>> searchTours(

            TourSearchRequest searchRequest,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "newest") String sortBy
    ) {

        int pageIndex = (page < 1) ? 0 : page - 1;


        Sort sort;
        switch (sortBy) {
            case "price_asc":

                sort = Sort.by(Sort.Direction.ASC, "id");
                break;
            case "price_desc":
                sort = Sort.by(Sort.Direction.DESC, "id");
                break;
            case "newest":
            default:
                sort = Sort.by(Sort.Direction.DESC, "id");
                break;
        }

        Pageable pageable = PageRequest.of(pageIndex, size, sort);

        PageResponse<TourResponse> result = tourService.searchTours(searchRequest, pageable);

        return ApiResponse.<PageResponse<TourResponse>>builder()
                .code(200)
                .message("Tìm kiếm tour thành công")
                .result(result)
                .build();
    }

    @GetMapping("/tours/select-list")
    public ApiResponse<List<TourSelectResponse>> getTourSelectList() {
        List<TourSelectResponse> list = tourRepository.getTourSelectList();
        return ApiResponse.<List<TourSelectResponse>>builder()
                .result(list)
                .build();
    }

    @PostMapping("/admin/tours/images/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> upload(@ModelAttribute @Valid TourImageRequest request) throws Exception {
        String fileUrl = tourImageService.uploadAndSave(request);

        return ApiResponse.<String>builder()
                .result(fileUrl)
                .message("Đã đẩy ảnh lên kho và lưu vào DB thành công!")
                .build();
    }
    @PostMapping("/admin/tours/images/upload-raw")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> uploadRaw(@RequestParam("file") MultipartFile file) throws Exception {

        String fileUrl = tourImageService.uploadOnlyToMinIO(file);

        return ApiResponse.<String>builder()
                .result(fileUrl)
                .message("Đã đẩy ảnh lên kho MinIO thành công!")
                .build();
    }
}